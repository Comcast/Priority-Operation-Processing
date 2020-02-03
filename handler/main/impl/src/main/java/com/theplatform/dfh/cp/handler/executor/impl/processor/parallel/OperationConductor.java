package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.messages.ExecutorMessages;
import com.theplatform.dfh.cp.handler.executor.impl.processor.JsonContextUpdater;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.OperationRunnerFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * Manages the execution of the operations, executing those with no remaining dependencies immediately.
 * There is no fixed limit on concurrent operations (at this time...)
 */
public class OperationConductor implements OnOperationCompleteListener
{
    private static Logger logger = LoggerFactory.getLogger(OperationConductor.class);

    protected static final String UNKNOWN_OPERATION_NAME = "UnknownOperation";
    protected static final String UNKNOWN_POD_NAME = "UnspecifiedPodName";

    private static final String THREAD_POOL_SIZE_SETTING = "operation.conductor.threadpool.size";
    private static final int DEFAULT_THREAD_POOL_SIZE = 50;

    // queue of operations that have been completed since the last readiness evaluation (this is the only collection accessed cross-thread)
    private BlockingQueue<OperationWrapper> postProcessingOperationQueue;

    // conductor tracking collections
    private List<OperationWrapper> pendingOperations;
    private List<OperationWrapper> runningOperations;
    private List<OperationWrapper> completedOperations;

    // This list is independent of the others
    private List<OperationWrapper> failedOperations;

    // read only list of all operations
    private final List<OperationWrapper> allOperations;

    private List<DiagnosticEvent> diagnosticEvents;

    private ExecutorContext executorContext;
    private OperationRunnerFactory operationRunnerFactory;
    private ExecutorService executorService;
    private JsonContextUpdater jsonContextUpdater;

    /**
     * Ctor
     * @param operations The operations to conduct (all will be run, unless there is a workflow problem)
     * @param executorContext The context for this executor instance
     */
    public OperationConductor(Collection<Operation> operations, ExecutorContext executorContext)
    {
        this.postProcessingOperationQueue = new LinkedBlockingQueue<>();
        // thread safety is not actually important on these collections. They are only adjusted/read in the OperationAdviser thread
        this.runningOperations = new ArrayList<>();
        this.completedOperations = new ArrayList<>();
        this.failedOperations = new ArrayList<>();

        this.executorContext = executorContext;
        this.jsonContextUpdater = new JsonContextUpdater(executorContext);
        this.operationRunnerFactory = new OperationRunnerFactory();

        diagnosticEvents = new LinkedList<>();

        this.pendingOperations = operations.stream().map(op -> new OperationWrapper(op).init(executorContext, jsonContextUpdater)).collect(Collectors.toList());
        this.allOperations = Collections.unmodifiableList(new ArrayList<>(pendingOperations));
    }

    /**
     * Main thread entry point. Will start pending operations once ready and monitor for all operations to be complete.
     */
    public void run()
    {
        final int ORIGINAL_OP_COUNT = pendingOperations.size();

        try
        {
            logger.debug("Loading Prior Progress (if available)");
            loadPriorProgress();

            logger.debug("Getting executor service");
            if(executorService == null)
            {
                executorService = Executors.newFixedThreadPool(
                    Integer.parseInt(executorContext.getLaunchDataWrapper().getPropertyRetriever().getField(THREAD_POOL_SIZE_SETTING, Integer.toString(DEFAULT_THREAD_POOL_SIZE))),
                    new OperationRunnerThreadFactory("OpRunner-"));
            }

            logger.debug("Adding progress");
            executorContext.getAgendaProgressReporter().addProgress(ProcessingState.EXECUTING, ExecutorMessages.OPERATIONS_RUNNING.getMessage());

            logger.debug("Before drain/launch operations.");
            while (!pendingOperations.isEmpty())
            {
                drainPostProcessOperations();
                launchReadyPendingOperations();
                waitOnPostProcessOperations();
            }
            while(!runningOperations.isEmpty())
            {
                drainPostProcessOperations();
                waitOnPostProcessOperations();
            }
        }
        catch(Throwable t)
        {
            diagnosticEvents.add(new DiagnosticEvent(ExecutorMessages.OPERATIONS_ERROR.getMessage(), t));
            logger.error(ExecutorMessages.OPERATIONS_ERROR.getMessage(), t);
        }
        finally
        {
            // TODO: log an error or something if these don't match
            logger.info("Original OpCount: {} Completed OpCount: {}", ORIGINAL_OP_COUNT, completedOperations.size());
            operationRunnerFactory.shutdown();
            List<Runnable> remainingTasks = executorService.shutdownNow();
            logger.info("ExecutorService shutdownNow called. {} Runnables were waiting.", remainingTasks.size());
        }
    }

    /**
     * Loads any existing progress into the OperationConductor
     */
    protected void loadPriorProgress()
    {
        AgendaProgress agendaProgress = executorContext.getLaunchDataWrapper().getLastProgressObject(AgendaProgress.class);

        if(agendaProgress == null
            || agendaProgress.getOperationProgress() == null
            || pendingOperations.size() == 0)
        {
            logger.debug("No prior progress loaded");
            return;
        }

        logger.debug("Processing prior progress");

        Map<String, OperationWrapper> operationWrapperMap = new HashMap<>();
        pendingOperations.forEach(opWrapper -> operationWrapperMap.put(opWrapper.getOperation().getName(), opWrapper));

        // Successful Ops
        Arrays.stream(agendaProgress.getOperationProgress())
            .filter(operationProgress -> operationProgress.getProcessingState() == ProcessingState.COMPLETE
                && StringUtils.equalsIgnoreCase(operationProgress.getProcessingStateMessage(), CompleteStateMessage.SUCCEEDED.name())
                && operationProgress.getOperation() != null)
            .forEach(operationProgress ->
            {
                OperationWrapper operationWrapper = operationWrapperMap.get(operationProgress.getOperation());
                if(operationWrapper != null)
                {
                    operationWrapper.setSuccess(true);
                    operationWrapper.setOutputPayload(operationProgress.getResultPayload());
                    postProcessCompletedOperation(operationWrapper);
                }
            });

        if(completedOperations.size() > 0)
        {
            logger.info("Completed Operation Progress found for operations: {}",
                completedOperations.stream().map(ow -> ow.getOperation().getName()).collect(Collectors.joining(",")));
            // increment the completed count in the reporter (no reporting will be performed for already completed operations, for now this is just a numeric)
            executorContext.getAgendaProgressReporter().incrementCompletedOperationCount(completedOperations.size());
        }

        // Incomplete / Failed Ops
        Arrays.stream(agendaProgress.getOperationProgress())
            .filter(operationProgress -> operationProgress.getProcessingState() != ProcessingState.COMPLETE
                && operationProgress.getOperation() != null)
            .forEach(operationProgress ->
            {
                OperationWrapper operationWrapper = operationWrapperMap.get(operationProgress.getOperation());
                if(operationWrapper != null)
                {
                    operationWrapper.setPriorExecutionOperationProgress(operationProgress);
                }
            });
    }

    /**
     * Starts any operations that are pending and ready
     */
    protected void launchReadyPendingOperations()
    {
        // get and run all the ready ops against the executor
        Collection<OperationWrapper> readyOperations = getReadyOperations();

        // if there is nothing ready and nothing running it's DEADLOCK time!
        if(runningOperations.size() == 0 && readyOperations.size() == 0)
        {
            logger.info("DEADLOCK{}{}", System.getProperty("line.separator"),
                pendingOperations.stream().map(ow -> ow.getOperation().getName() + ":(depends on):" + String.join(",", ow.getDependencies()))
                    .collect(Collectors.joining(System.getProperty("line.separator"))));
            throw new AgendaExecutorException("Agenda operation deadlock has occurred. There are operations with dependencies but nothing is running.");
        }

        if(readyOperations.size() > 0)
        {
            logger.info(
                "Starting the following operations: {}",
                readyOperations.stream().map(ow -> ow.getOperation().getName()).collect(Collectors.joining(", ")));

            readyOperations.forEach(operationWrapper ->
                {
                    try
                    {
                        transitionStartedOperation(operationWrapper);
                        executorService.submit(operationRunnerFactory.createOperationRunner(operationWrapper, executorContext, this));
                    }
                    catch(Exception e)
                    {
                        throw new RuntimeException(
                            ExecutorMessages.OPERATION_EXECUTION_ERROR.getMessage(operationWrapper.getOperation().getName()),
                            e);
                    }
                }
            );
        }
    }

    /**
     * Waits for a short period of time for any operations to complete. If any are queued complete this method will exit immediately.
     */
    protected void waitOnPostProcessOperations()
    {
        // do not wait if there is no reason to
        if(pendingOperations.size() == 0 && runningOperations.size() == 0) return;

        try
        {
            // wait on the queue to change, after some period of time we may want a wake up
            // this action will pull an item off the queue so it is critical that operation goes through post processing.
            postProcessCompletedOperation(postProcessingOperationQueue.poll(5, TimeUnit.SECONDS));
        }
        catch(InterruptedException e)
        {
            logger.error("Thread interrupt", e);
            throw new RuntimeException("Thread was interrupted. Time to stop processing.");
            // TODO: react
        }
    }

    /**
     * Drains the queue of completed operations from the running operations collection
     */
    protected void drainPostProcessOperations()
    {
        // wipe the completed operation queue just before gathering the newly ready operations
        LinkedList<OperationWrapper> recentCompletedOperations = new LinkedList<>();
        postProcessingOperationQueue.drainTo(recentCompletedOperations);

        logger.info(
            "clearing the following complete operations: {}",
            recentCompletedOperations.stream().map(ow -> ow.getOperation().getName()).collect(Collectors.joining(", ")));

        // post process all drained operations
        recentCompletedOperations.forEach(this::postProcessCompletedOperation);
    }

    /**
     * Post processes the completed operation and removes the operation from the running operations collection (transitioning to completed)
     * @param operationWrapper the operation to remove from the running operations
     */
    protected void postProcessCompletedOperation(OperationWrapper operationWrapper)
    {
        if(operationWrapper != null)
        {
            jsonContextUpdater.onComplete(operationWrapper);

            if(!operationWrapper.getSuccess())
            {
                failedOperations.add(operationWrapper);
                logger.warn("{} operation failed. Clearing all pending operations.", operationWrapper.getOperation().getName());
                // remove all pending operations
                pendingOperations.clear();
            }

            // this operation should migrate to complete no matter its current state
            pendingOperations.remove(operationWrapper);
            runningOperations.remove(operationWrapper);
            completedOperations.add(operationWrapper);
        }
    }

    /**
     * Adds the operations to the running operations
     * @param operationWrapper the operation to add from the running operations
     */
    protected void transitionStartedOperation(OperationWrapper operationWrapper)
    {
        if(operationWrapper != null)
        {
            pendingOperations.remove(operationWrapper);
            runningOperations.add(operationWrapper);
        }
    }

    /**
     * Handles the completion of an operation. Simply queues the operation for post processing
     * @param operationWrapper The operation completed
     */
    @Override
    public void onComplete(OperationWrapper operationWrapper)
    {
        logger.info("Operation onComplete: {}", operationWrapper.getOperation().getName());
        postProcessingOperationQueue.add(operationWrapper);
    }

    /**
     * Gets all of the ready operations from the pending collection
     * @return Collection of zero or more ready operations
     */
    protected Collection<OperationWrapper> getReadyOperations()
    {
        Set<String> completedOperationNames = completedOperations.stream().map(x -> x.getOperation().getName()).collect(Collectors.toSet());
        // get all the operations that are ready to run
        return pendingOperations.stream().filter(opWrapper -> opWrapper.isReady(executorContext, completedOperationNames)).collect(Collectors.toList());
    }

    // below exposed for unit testing

    protected void setExecutorService(ExecutorService executorService)
    {
        this.executorService = executorService;
    }

    protected void setOperationRunnerFactory(OperationRunnerFactory operationRunnerFactory)
    {
        this.operationRunnerFactory = operationRunnerFactory;
    }

    protected BlockingQueue<OperationWrapper> getPostProcessingOperationQueue()
    {
        return postProcessingOperationQueue;
    }

    protected List<OperationWrapper> getRunningOperations()
    {
        return runningOperations;
    }

    protected List<OperationWrapper> getPendingOperations()
    {
        return pendingOperations;
    }

    /**
     * Sets the pendingOperations. This should only be used for unit testing.
     * @param pendingOperations The pending operations
     */
    protected void setPendingOperations(List<OperationWrapper> pendingOperations)
    {
        this.pendingOperations = pendingOperations;
    }

    protected List<OperationWrapper> getCompletedOperations()
    {
        return completedOperations;
    }

    /**
     * Gets all the diagnostic events across all of the operation wrappers and any created by the conductor
     * @return List of diagnostic events or null if none present.
     */
    public List<DiagnosticEvent> retrieveAllDiagnosticEvents()
    {
        List<DiagnosticEvent> operationDiagnosticEvents = allOperations.stream()
            .filter(po -> po.getDiagnosticEvents() != null && po.getDiagnosticEvents().size() > 0)
            .flatMap(po -> po.getDiagnosticEvents().stream())
            .collect(Collectors.toList());
        operationDiagnosticEvents.addAll(diagnosticEvents);
        return operationDiagnosticEvents.size() == 0 ? null : operationDiagnosticEvents;
    }

    /**
     * Indicates if any operations have failed. This method should only be used after the conclusion of execution.
     * @return Indicator of any failed operations.
     */
    public boolean hasExecutionFailed()
    {
        return failedOperations.size() > 0 || diagnosticEvents.size() > 0;
    }

    public String getFailedOperationsDelimited(String delimiter)
    {
        return failedOperations.stream()
            .map(operationWrapper ->
                {
                    String operationName = operationWrapper.getOperation() == null
                        ? UNKNOWN_OPERATION_NAME
                        : operationWrapper.getOperation().getName();

                    String executorIdentifier = operationWrapper.getOperationExecutor() == null
                        ? UNKNOWN_POD_NAME
                        : operationWrapper.getOperationExecutor().getIdenitifier();

                    return String.format(
                        "%1$s[%2$s]",
                        operationName,
                        executorIdentifier);
                }
            )
            .collect(Collectors.joining(delimiter));
    }

    protected void setFailedOperations(List<OperationWrapper> failedOperations)
    {
        this.failedOperations = failedOperations;
    }
}
