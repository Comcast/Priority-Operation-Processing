package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.translator.JsonPayloadTranslator;
import com.theplatform.dfh.cp.handler.base.translator.PayloadTranslationResult;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.messages.ExecutorMessages;
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

        PayloadTranslationResult<ExecutorHandlerInput> translationResult = new JsonPayloadTranslator<ExecutorHandlerInput>(jsonHelper)
            .translatePayload(launchDataWrapper.getPayload(), ExecutorHandlerInput.class);
        if(!translationResult.isSuccessful())
        {
            agendaProgressReporter.addFailed(translationResult.getDiagnosticEvent());
            return;
        }

        ExecutorHandlerInput handlerInput = translationResult.getObject();

        if (handlerInput == null || handlerInput.getOperations() == null)
        {
            agendaProgressReporter.addFailed(new DiagnosticEvent(ExecutorMessages.AGENDA_NO_OPERATIONS.getMessage()));
            return;
        }

        try
        {
            agendaProgressReporter.addProgress(ProcessingState.EXECUTING, ExecutorMessages.OPERATIONS_RUNNING.getMessage());
            handlerInput.getOperations().forEach(op -> executeOperation(op, agendaProgressReporter));
            agendaProgressReporter.addSucceeded();
        }
        catch (AgendaExecutorException e)
        {
            logger.error(ExecutorMessages.OPERATIONS_ERROR.getMessage(), e);
            agendaProgressReporter.addFailed(new DiagnosticEvent(ExecutorMessages.OPERATIONS_ERROR.getMessage(), e));
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
