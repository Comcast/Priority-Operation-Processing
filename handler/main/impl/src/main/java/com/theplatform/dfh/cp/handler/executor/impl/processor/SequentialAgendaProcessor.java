package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.OperationRunnerFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Basic test/local/prototype processor for running the Agenda
 * TODO: this can be completely replaced with the ParallelOperationAgendaProcessor (by specifying a thread pool of 1)
 */
public class SequentialAgendaProcessor extends BaseAgendaProcessor
{
    private static Logger logger = LoggerFactory.getLogger(SequentialAgendaProcessor.class);

    private OperationRunnerFactory operationRunnerFactory;
    private JsonContextUpdater jsonContextUpdater;
    private Set<OperationWrapper> completedOperations;

    public SequentialAgendaProcessor(LaunchDataWrapper launchDataWrapper, ExecutorContext executorContext)
    {
        super(launchDataWrapper, executorContext);
        operationRunnerFactory = new OperationRunnerFactory();
        jsonContextUpdater = new JsonContextUpdater(executorContext);
        completedOperations = new HashSet<>();
    }

    /**
     * Executes the ops in the Agenda in order
     * @return
     */
    public Void execute()
    {
        AgendaProgressReporter agendaProgressReporter = executorContext.getAgendaProgressReporter();

        agendaProgressReporter.addProgress(ProcessingState.EXECUTING, "Loading Agenda");
        ExecutorHandlerInput handlerInput = null;
        try
        {
            handlerInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), ExecutorHandlerInput.class);
            agendaProgressReporter.addProgress(ProcessingState.EXECUTING, "Agenda Loaded");
            // TODO: this is only temporary because the linkid has to be set on every update
            if(handlerInput.getLinkId() != null)
                agendaProgressReporter.getAgendaProgressFactory().setLinkId(handlerInput.getLinkId());
        }
        catch (Exception e)
        {
            agendaProgressReporter.addFailed("Invalid input. Failed to load payload.");
            return null;
        }

        if (handlerInput == null)
        {
            agendaProgressReporter.addFailed("Invalid input. No payload.");
            return null;
        }

        if (handlerInput.getOperations() == null)
        {
            agendaProgressReporter.addFailed("No operations in Agenda. Nothing to do.");
            return null;
        }

        try
        {
            agendaProgressReporter.addProgress(ProcessingState.EXECUTING, "Running Operations");
            handlerInput.getOperations().forEach(op -> executeOperation(op, agendaProgressReporter));
            agendaProgressReporter.addSucceeded(null);
        }
        catch (AgendaExecutorException e)
        {
            logger.error("Error running operations.", e);
            // TODO: some diagnostic back...
            agendaProgressReporter.addFailed(null);
        }
        return null;
    }

    /**
     * Executes the specific operation on the current thread
     * @param operation The operation to execute
     */
    protected void executeOperation(Operation operation, AgendaProgressReporter agendaProgressReporter)
    {
        OperationWrapper operationWrapper = new OperationWrapper(operation).init(executorContext, jsonContextUpdater);
        Set<String> completedOperationNames = completedOperations.stream().map(x -> x.getOperation().getName()).collect(Collectors.toSet());
        if(operationWrapper.isReady(executorContext, completedOperationNames))
        {
            agendaProgressReporter.addProgress(ProcessingState.EXECUTING, String.format("Sequential Op: Running %1$s", operation.getName()));
            operationRunnerFactory.createOperationRunner(operationWrapper, executorContext, new OnOperationCompleteListener()
            {
                @Override
                public void onComplete(OperationWrapper operationWrapper)
                {
                    jsonContextUpdater.onComplete(operationWrapper);
                    completedOperations.add(operationWrapper);
                }
            }).run();
        }
        else
        {
            throw new AgendaExecutorException("An operation dependency is missing. The sequential executor does not support this.");
        }
    }

    public void setOperationRunnerFactory(OperationRunnerFactory operationRunnerFactory)
    {
        this.operationRunnerFactory = operationRunnerFactory;
    }

}
