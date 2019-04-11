package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.translator.JsonPayloadTranslator;
import com.theplatform.dfh.cp.handler.base.translator.PayloadTranslationResult;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.messages.ExecutorMessages;
import com.theplatform.dfh.cp.handler.executor.impl.processor.BaseAgendaProcessor;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AgendaProcessor that will execute the operations in parallel where possible.
 */
public class ParallelOperationAgendaProcessor extends BaseAgendaProcessor
{
    private static Logger logger = LoggerFactory.getLogger(ParallelOperationAgendaProcessor.class);

    private OperationConductorFactory operationAdviserFactory;

    public ParallelOperationAgendaProcessor(ExecutorContext executorContext)
    {
        super(executorContext);
        this.operationAdviserFactory = new OperationConductorFactory();
    }

    protected void doExecute()
    {
        AgendaProgressReporter agendaProgressReporter = operationContext.getAgendaProgressReporter();

        PayloadTranslationResult<ExecutorHandlerInput> translationResult = new JsonPayloadTranslator<ExecutorHandlerInput>()
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
            OperationConductor operationConductor = operationAdviserFactory.createOperationConductor(handlerInput.getOperations(), operationContext);
            operationConductor.run();
            if(operationConductor.haveAnyOperationsFailed())
                agendaProgressReporter.addFailed(operationConductor.retrieveAllDiagnosticEvents());
            else
                agendaProgressReporter.addSucceeded();
        }
        catch (AgendaExecutorException e)
        {
            // TODO: need to create a diganostic for the executor...
            agendaProgressReporter.addFailed(new DiagnosticEvent(ExecutorMessages.OPERATIONS_ERROR.getMessage(), e));
            logger.error("", e);
        }
    }

}
