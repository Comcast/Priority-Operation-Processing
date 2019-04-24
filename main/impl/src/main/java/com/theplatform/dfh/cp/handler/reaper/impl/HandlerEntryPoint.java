package com.theplatform.dfh.cp.handler.reaper.impl;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubernetesLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reaper.impl.context.ReaperContext;
import com.theplatform.dfh.cp.handler.reaper.impl.context.ReaperContextFactory;
import com.theplatform.dfh.cp.handler.reaper.impl.processor.ReaperProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerEntryPoint extends BaseHandlerEntryPoint<ReaperContext, ReaperProcessor, KubernetesLaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(HandlerEntryPoint.class);

    public HandlerEntryPoint(String[] args)
    {
        super(args);

    }

    /**
     * Debugging/running with a local-only build use these args:
     * -launchType local -externalLaunchType local -propFile ./handler/main/package/local/config/external.properties -payloadFile ./handler/main/package/local/payload.json
     *
     * Debugging running local with minikube for pod launches
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external-minikube.properties -payloadFile ./handler/main/package/local/payload.json
     *
     * Debugging/running local with kubernetes for pod launches: (requires environment vars for k8s auth)
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external.properties -payloadFile ./handler/main/package/local/payload.json
     *
     * @param args command line args
     */
    public static void main(String[] args)
    {
        //logger.info(String.join("\n", args));
        new HandlerEntryPoint(args).execute();
        logger.info("Pod Reaper Complete");
    }

    @Override
    protected KubernetesLaunchDataWrapper createLaunchDataWrapper(String[] args)
    {
        return new KubernetesLaunchDataWrapper(args);
    }

    @Override
    protected BaseOperationContextFactory<ReaperContext> createOperationContextFactory(KubernetesLaunchDataWrapper launchDataWrapper)
    {
        return new ReaperContextFactory(launchDataWrapper);
    }

    @Override
    protected ReaperProcessor createHandlerProcessor(ReaperContext executorContext)
    {
        return new ReaperProcessor(executorContext);
    }
}
