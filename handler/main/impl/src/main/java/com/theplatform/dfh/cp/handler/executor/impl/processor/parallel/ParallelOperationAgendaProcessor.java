package com.theplatform.dfh.cp.handler.executor.impl.processor.parallel;

import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.processor.BaseAgendaProcessor;
import com.theplatform.dfh.cp.handler.executor.impl.progress.ProgressStatusUpdaterFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
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
        this(launchDataWrapper, executorContext, new ProgressStatusUpdaterFactory(launchDataWrapper));
    }

    public ParallelOperationAgendaProcessor(LaunchDataWrapper launchDataWrapper, ExecutorContext executorContext, ProgressStatusUpdaterFactory progressStatusUpdaterFactory)
    {
        super(launchDataWrapper, executorContext, progressStatusUpdaterFactory);
        this.operationAdviserFactory = new OperationConductorFactory();
    }

    /**
     * Executes the Agenda processing
     * @return null (always)
     */
    @Override
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
            // TODO: this could run in a thread itself... if it matters
            // TODO: progress reporting will need to have some hooks into this
            operationAdviserFactory.createOperationConductor(handlerInput.getOperations(), executorContext).run();
        }
        catch (AgendaExecutorException e)
        {
            executorContext.getReporter().reportFailure("", e);
            logger.error("", e);
        }

        // TODO: remove this when progress reporting is real
        progressStatusUpdaterFactory.createProgressStatusUpdater(handlerInput).updateProgress();
        return null;
    }

}
