package com.theplatform.dfh.cp.handler.sample.impl;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.sample.impl.context.OperationContext;
import com.theplatform.dfh.cp.handler.sample.impl.context.OperationContextFactory;
import com.theplatform.dfh.cp.handler.sample.impl.processor.SampleActionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerEntryPoint extends BaseHandlerEntryPoint<OperationContext, SampleActionProcessor, DefaultLaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(HandlerEntryPoint.class);

    public HandlerEntryPoint(String[] args)
    {
        super(args);
    }

    /**
     * Local/IDEA non-docker execution prerequisites:
     * - debugging/running with a local-only build use these args (will definitely need to adjust the payload.json accordingly):
     * -launchType local -propFile ./handler/main/package/local/config/external.properties -payloadFile ./handler/main/package/local/payload.json
     *
     * @param args command line args
     */
    public static void main(String[] args)
    {
        // just for convenience...
        // if(args != null) System.out.println(String.format("ARGS: %1$s", String.join(",", args)));
        //System.out.println(System.getProperty("user.dir"));
        new HandlerEntryPoint(args).execute();
    }

    @Override
    protected DefaultLaunchDataWrapper createLaunchDataWrapper(String[] args)
    {
        return new DefaultLaunchDataWrapper(args);
    }

    @Override
    protected BaseOperationContextFactory<OperationContext> createOperationContextFactory(DefaultLaunchDataWrapper launchDataWrapper)
    {
        return new OperationContextFactory(launchDataWrapper);
    }

    @Override
    protected SampleActionProcessor createHandlerProcessor(DefaultLaunchDataWrapper launchDataWrapper, OperationContext operationContext)
    {
        return new SampleActionProcessor(launchDataWrapper, operationContext);
    }
}
