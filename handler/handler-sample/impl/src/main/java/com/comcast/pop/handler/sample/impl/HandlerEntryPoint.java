package com.comcast.pop.handler.sample.impl;

import com.comcast.pop.handler.sample.impl.context.OperationContext;
import com.comcast.pop.handler.sample.impl.context.OperationContextFactory;
import com.comcast.pop.handler.sample.impl.processor.SampleActionProcessor;
import com.comast.pop.handler.base.BaseHandlerEntryPoint;
import com.comast.pop.handler.base.context.BaseOperationContextFactory;
import com.comcast.pop.handler.kubernetes.support.config.KubernetesLaunchDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerEntryPoint extends BaseHandlerEntryPoint<OperationContext, SampleActionProcessor, KubernetesLaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(HandlerEntryPoint.class);

    public HandlerEntryPoint(String[] args)
    {
        super(args);
    }

    /**
     * Local/IDEA non-docker execution prerequisites:
     * - debugging/running with a local-only build use these args (will definitely need to adjust the payload.json accordingly):
     * -launchType local -externalLaunchType local -propFile ./handler/handler-sample-impl/package/local/config/external.properties -payloadFile ./handler/handler-sample-impl/package/local/payload.json
     *
     * (to specify auth token files) For kubernetes external execution use -oauthCertPath [full file path] -oauthTokenPath [full file path]
     *
     * Local handler / Kubernetes external execution
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/handler-sample-impl/package/local/config/external.properties -payloadFile ./handler/handler-sample-impl/package/local/payload.json
     *
     * @param args command line args
     */
    public static void main(String[] args)
    {
        // just for convenience...
        // if(args != null) System.out.println(String.format("ARGS: %1$s", String.join(",", args)));
        // System.out.println(System.getProperty("user.dir"));
        new HandlerEntryPoint(args).execute();
    }

    @Override
    protected KubernetesLaunchDataWrapper createLaunchDataWrapper(String[] args)
    {
        return new KubernetesLaunchDataWrapper(args);
    }

    @Override
    protected BaseOperationContextFactory<OperationContext> createOperationContextFactory(KubernetesLaunchDataWrapper launchDataWrapper)
    {
        return new OperationContextFactory(launchDataWrapper);
    }

    @Override
    protected SampleActionProcessor createHandlerProcessor(OperationContext operationContext)
    {
        return new SampleActionProcessor(operationContext);
    }
}
