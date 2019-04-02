package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.OperationRunnerFactory;
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

    public SequentialAgendaProcessor(ExecutorContext executorContext)
    {
        super(executorContext);
        operationRunnerFactory = new OperationRunnerFactory();
        jsonContextUpdater = new JsonContextUpdater(executorContext);
        completedOperations = new HashSet<>();
    }

    /**
     * Executes the ops in the Agenda in order
     */
    protected void doExecute()
    {
        AgendaProgressReporter agendaProgressReporter = operationContext.getAgendaProgressReporter();

        agendaProgressReporter.addProgress(ProcessingState.EXECUTING, "Loading Agenda");
        ExecutorHandlerInput handlerInput = null;
        try
        {
            handlerInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), ExecutorHandlerInput.class);
            agendaProgressReporter.addProgress(ProcessingState.EXECUTING, "Agenda Loaded");
        }
        catch (Exception e)
        {
            agendaProgressReporter.addFailed("Invalid input. Failed to load payload.");
            return;
        }

        if (handlerInput == null)
        {
            agendaProgressReporter.addFailed("Invalid input. No payload.");
            return;
        }

        if (handlerInput.getOperations() == null)
        {
            agendaProgressReporter.addFailed("No operations in Agenda. Nothing to do.");
            return;
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
    }

    /**
     * Executes the specific operation on the current thread
     * @param operation The operation to execute
     */
    protected void executeOperation(Operation operation, AgendaProgressReporter agendaProgressReporter)
    {
        OperationWrapper operationWrapper = new OperationWrapper(operation).init(operationContext, jsonContextUpdater);
        Set<String> completedOperationNames = completedOperations.stream().map(x -> x.getOperation().getName()).collect(Collectors.toSet());
        if(operationWrapper.isReady(operationContext, completedOperationNames))
        {
            agendaProgressReporter.addProgress(ProcessingState.EXECUTING, String.format("Sequential Op: Running %1$s", operation.getName()));
            operationRunnerFactory.createOperationRunner(operationWrapper, operationContext, new OnOperationCompleteListener()
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
