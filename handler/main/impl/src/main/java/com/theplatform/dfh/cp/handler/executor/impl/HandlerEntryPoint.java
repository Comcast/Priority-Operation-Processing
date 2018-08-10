package com.theplatform.dfh.cp.handler.executor.impl;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.executor.impl.context.OperationContext;
import com.theplatform.dfh.cp.handler.executor.impl.context.OperationContextFactory;
import com.theplatform.dfh.cp.handler.executor.impl.processor.AgendaExecutorProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerEntryPoint extends BaseHandlerEntryPoint<OperationContext, AgendaExecutorProcessor>
{
    private static Logger logger = LoggerFactory.getLogger(HandlerEntryPoint.class);

    public HandlerEntryPoint(String[] args)
    {
        super(args);

    }

    /**
     * TODO: this information may be out of date (fix when implementing)
     * - debugging/running with a local-only build use these args (will definitely need to adjust the payload.json accordingly):
     * -externalLaunchType local -propFile ./handler/main/package/local/config/external.properties -payloadFile ./handler/main/package/local/payload.json
     *
     * @param args command line args
     */
    public static void main(String[] args)
    {
        //logger.debug(System.getProperty("user.dir"));
        logger.info(String.join("\n", args));
        new HandlerEntryPoint(args).execute();
    }

    @Override
    protected LaunchDataWrapper createLaunchDataWrapper(String[] args)
    {
        return new DefaultLaunchDataWrapper(args);
    }

    @Override
    protected BaseOperationContextFactory<OperationContext> createOperationContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        return new OperationContextFactory(launchDataWrapper);
    }

    @Override
    protected AgendaExecutorProcessor createHandlerProcessor(LaunchDataWrapper launchDataWrapper, OperationContext operationContext)
    {
        return new AgendaExecutorProcessor(launchDataWrapper, operationContext);
    }
}
