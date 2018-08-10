package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.OperationContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.jsonhelper.JsonHelper;

/**
 * Basic processor for running mediainfo and requesting the output is parsed
 */
public class AgendaExecutorProcessor implements HandlerProcessor<Void>
{
    private LaunchDataWrapper launchDataWrapper;
    private OperationContext operationContext;
    private JsonHelper jsonHelper;

    public AgendaExecutorProcessor(LaunchDataWrapper launchDataWrapper, OperationContext operationContext)
    {
        this.launchDataWrapper = launchDataWrapper;
        this.operationContext = operationContext;
        this.jsonHelper = new JsonHelper();
    }

    /**
     * Executes the necessary steps to get the MediaProperties for the file
     * @return
     */
    public Void execute()
    {
        ExecutorHandlerInput handlerInput;
        try
        {
            handlerInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), ExecutorHandlerInput.class);
            operationContext.getReporter().reportProgress(handlerInput);
        }
        catch(Exception e)
        {
            throw new AgendaExecutorException("Failed to load payload.", e);
        }
        operationContext.getReporter().reportSuccess("Done!");
        return null;
    }
}
