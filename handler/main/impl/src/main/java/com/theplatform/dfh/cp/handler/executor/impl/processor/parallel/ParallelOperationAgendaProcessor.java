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
            logger.debug("Adding progress");
            agendaProgressReporter.addProgress(ProcessingState.EXECUTING, ExecutorMessages.OPERATIONS_RUNNING.getMessage());
            logger.debug("Running operation");
            OperationConductor operationConductor = operationConductorFactory.createOperationConductor(handlerInput.getOperations(), operationContext);
            operationConductor.run();
            if(operationConductor.hasExecutionFailed())
            {
                logger.error("Execution failed");
                agendaProgressReporter.addFailed(operationConductor.retrieveAllDiagnosticEvents());
            }
            else
            {
                agendaProgressReporter.addSucceeded();
                logger.debug("Execute successful");
            }
        }
        catch (AgendaExecutorException e)
        {
            agendaProgressReporter.addFailed(new DiagnosticEvent(ExecutorMessages.OPERATIONS_ERROR.getMessage(), e));
            logger.error("", e);
        }
    }

}
