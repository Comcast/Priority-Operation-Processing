package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.processor.JsonContextUpdater;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OnOperationCompleteListener;
import com.theplatform.dfh.cp.handler.executor.impl.processor.OperationWrapper;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.OperationRunnerFactory;
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

    private static final String THREAD_POOL_SIZE_SETTING = "operation.conductor.threadpool.size";
    private static final int DEFAULT_THREAD_POOL_SIZE = 50;

    // queue of operations that have been completed since the last readiness evaluation (this is the only collection accessed cross-thread)
    private BlockingQueue<OperationWrapper> postProcessingOperationQueue;

    // conductor tracking collections
    private Collection<OperationWrapper> pendingOperations;
    private Collection<OperationWrapper> runningOperations;
    private Collection<OperationWrapper> completedOperations;

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

        this.executorContext = executorContext;
        this.jsonContextUpdater = new JsonContextUpdater(executorContext);
        this.operationRunnerFactory = new OperationRunnerFactory();

        this.pendingOperations = operations.stream().map(op -> new OperationWrapper(op).init(executorContext, jsonContextUpdater)).collect(Collectors.toList());
    }

    /**
     * Main thread entry point. Will start pending operations once ready and monitor for all operations to be complete.
     */
    public void run()
    {

        final int ORIGINAL_OP_COUNT = pendingOperations.size();

        try
        {
            executorContext.getAgendaProgressReporter().addProgress(ProcessingState.EXECUTING, "Initializing Operation ThreadPool");

            if(executorService == null)
            {
                executorService = Executors.newFixedThreadPool(Integer.parseInt(executorContext.getLaunchDataWrapper().getPropertyRetriever().getField(THREAD_POOL_SIZE_SETTING,
                    Integer.toString(DEFAULT_THREAD_POOL_SIZE))));
            }

            executorContext.getAgendaProgressReporter().addProgress(ProcessingState.EXECUTING, "Launching Operations");

            // TODO: need to react to failed operations (probably halt remaining)
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
            logger.error("Failed to run operations.", t);
        }
        finally
        {
            // TODO: log an error or something if these don't match
            logger.info("Original OpCount: {} Completed OpCount: {}", ORIGINAL_OP_COUNT, completedOperations.size());
            executorService.shutdown();
        }
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
                            String.format("Unable to launch operation: %1$s", operationWrapper.getOperation().getName()),
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

    protected Collection<OperationWrapper> getRunningOperations()
    {
        return runningOperations;
    }

    protected Collection<OperationWrapper> getPendingOperations()
    {
        return pendingOperations;
    }

    protected Collection<OperationWrapper> getCompletedOperations()
    {
        return completedOperations;
    }
}
