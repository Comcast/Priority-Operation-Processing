package com.comcast.pop.handler.executor.impl.processor.parallel;

import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.exception.AgendaExecutorException;
import com.comcast.pop.handler.executor.impl.messages.ExecutorMessages;
import com.comcast.pop.handler.executor.impl.processor.BaseAgendaProcessor;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.ProcessingState;
import com.comast.pop.handler.base.translator.JsonPayloadTranslator;
import com.comast.pop.handler.base.translator.PayloadTranslationResult;
import com.comcast.pop.handler.executor.api.ExecutorHandlerInput;
import com.comcast.pop.handler.executor.impl.progress.agenda.AgendaProgressReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AgendaProcessor that will execute the operations in parallel where possible.
 */
public class ParallelOperationAgendaProcessor extends BaseAgendaProcessor
{
    private static Logger logger = LoggerFactory.getLogger(ParallelOperationAgendaProcessor.class);
    private static final String METRICS_ENABLED_PROPERTY_KEY = "metrics.enabled";

    private OperationConductorFactory operationConductorFactory;

    public ParallelOperationAgendaProcessor(ExecutorContext executorContext)
    {
        super(executorContext);
        if (executorContext.getLaunchDataWrapper().getPropertyRetriever() != null &&
            executorContext.getLaunchDataWrapper().getPropertyRetriever().getBoolean(METRICS_ENABLED_PROPERTY_KEY, false))
        {
            this.operationConductorFactory = new MonitoringOperationConductorFactory(executorContext);
        }
        else
        {
            this.operationConductorFactory = new OperationConductorFactory();
        }
    }
    protected void doExecute()
    {
        logger.debug("Getting progress reporter");
        AgendaProgressReporter agendaProgressReporter = operationContext.getAgendaProgressReporter();

        logger.debug("Translating payload");
        PayloadTranslationResult<ExecutorHandlerInput> translationResult = new JsonPayloadTranslator<ExecutorHandlerInput>()
            .translatePayload(launchDataWrapper.getPayload(), ExecutorHandlerInput.class);
        if(!translationResult.isSuccessful())
        {
            logger.error("Failure to translate payload");
            agendaProgressReporter.addFailed(translationResult.getDiagnosticEvent());
            return;
        }

        logger.debug("Getting executor handler input");
        ExecutorHandlerInput handlerInput = translationResult.getObject();

        if (handlerInput == null || handlerInput.getOperations() == null)
        {
            logger.error("Failed to get handler input");
            agendaProgressReporter.addFailed(new DiagnosticEvent(ExecutorMessages.AGENDA_NO_OPERATIONS.getMessage()));
            return;
        }

        try
        {
            // post agenda load setup these fields for use across the app
            operationContext.setAgendaId(handlerInput.getId());
            if(handlerInput.getProgressId() != null)
                operationContext.setAgendaProgressId(handlerInput.getProgressId());
            operationContext.setAgenda(handlerInput);

            logger.debug("Adding progress");
            agendaProgressReporter.addProgress(ProcessingState.EXECUTING, ExecutorMessages.OPERATIONS_RUNNING.getMessage());
            logger.debug("Running operation");
            agendaProgressReporter.setOperationTotalCount(handlerInput.getOperations().size());
            OperationConductor operationConductor = operationConductorFactory.createOperationConductor(handlerInput.getOperations(), operationContext);
            operationConductor.run();
            if(operationConductor.hasExecutionFailed())
            {
                logger.error("Execution failed. Failed operations: {}", operationConductor.getFailedOperationsDelimited(","));
                agendaProgressReporter.addFailed(operationConductor.retrieveAllDiagnosticEvents());
            }
            else
            {
                agendaProgressReporter.addSucceeded();
                logger.debug("Execution successful");
            }
        }
        catch (AgendaExecutorException e)
        {
            agendaProgressReporter.addFailed(new DiagnosticEvent(ExecutorMessages.OPERATIONS_ERROR.getMessage(), e));
            logger.error("", e);
        }
    }

}
