package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.processor.runner.OperationRunnerFactory;
import com.theplatform.dfh.cp.handler.executor.impl.progress.ProgressStatusUpdaterFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
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

    public SequentialAgendaProcessor(LaunchDataWrapper launchDataWrapper, ExecutorContext executorContext, ProgressStatusUpdaterFactory progressStatusUpdaterFactory)
    {
        super(launchDataWrapper, executorContext, progressStatusUpdaterFactory);
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
        ExecutorHandlerInput handlerInput;
        try
        {
            handlerInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), ExecutorHandlerInput.class);
            executorContext.getReporter().reportProgress(handlerInput);
        }
        catch(Exception e)
        {
            throw new AgendaExecutorException("Failed to load payload.", e);
        }

        if(handlerInput == null)
        {
            executorContext.getReporter().reportFailure("Invalid input. No payload.", null);
            return null;
        }

        if(handlerInput.getOperations() == null)
        {
            executorContext.getReporter().reportFailure("No operations in Agenda. Nothing to do.", null);
            return null;
        }

        try
        {
            handlerInput.getOperations().forEach(this::executeOperation);
            executorContext.getReporter().reportSuccess("Done!");
        }
        catch (AgendaExecutorException e)
        {
            executorContext.getReporter().reportFailure("", e);
            logger.error("", e);
        }

        progressStatusUpdaterFactory.createProgressStatusUpdater(handlerInput).updateProgress();
        return null;
    }

    /**
     * Executes the specific operation on the current thread
     * @param operation The operation to execute
     */
    protected void executeOperation(Operation operation)
    {
        OperationWrapper operationWrapper = new OperationWrapper(operation).init(executorContext, jsonContextUpdater);
        Set<String> completedOperationNames = completedOperations.stream().map(x -> x.getOperation().getName()).collect(Collectors.toSet());
        if(operationWrapper.isReady(executorContext, completedOperationNames))
        {
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
