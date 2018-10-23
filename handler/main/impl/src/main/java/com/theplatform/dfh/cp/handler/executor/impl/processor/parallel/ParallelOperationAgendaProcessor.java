package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.processor.BaseAgendaProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
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

    public ParallelOperationAgendaProcessor(LaunchDataWrapper launchDataWrapper, ExecutorContext executorContext)
    {
        super(launchDataWrapper, executorContext);
        this.operationAdviserFactory = new OperationConductorFactory();
    }

    /**
     * Executes the Agenda processing
     * @return null (always)
     */
    @Override
    public Void execute()
    {
        executorContext.init();

        ExecutorHandlerInput handlerInput;
        try
        {
            AgendaProgressReporter agendaProgressReporter = executorContext.getAgendaProgressReporter();
            try
            {
                agendaProgressReporter.updateState(ProcessingState.EXECUTING, "Loading Agenda");
                handlerInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), ExecutorHandlerInput.class);
                agendaProgressReporter.updateState(ProcessingState.EXECUTING, "Agenda Loaded");
            }
            catch (Exception e)
            {
                throw new AgendaExecutorException("Failed to load payload.", e);
            }

            if (handlerInput == null)
            {
                agendaProgressReporter.updateState(ProcessingState.COMPLETE, "Invalid input. No payload.");
                return null;
            }

            if (handlerInput.getOperations() == null)
            {
                agendaProgressReporter.updateState(ProcessingState.COMPLETE, "No operations in Agenda. Nothing to do.");
                return null;
            }

            try
            {
                operationAdviserFactory.createOperationConductor(handlerInput.getOperations(), executorContext).run();
                agendaProgressReporter.updateState(ProcessingState.COMPLETE, "Done");
            }
            catch (AgendaExecutorException e)
            {
                // TODO: need to create a diganostic for the executor...
                agendaProgressReporter.updateState(ProcessingState.COMPLETE, "Failed");
                logger.error("", e);
            }
        }
        finally
        {
            executorContext.shutdown();
        }

        return null;
    }

}
