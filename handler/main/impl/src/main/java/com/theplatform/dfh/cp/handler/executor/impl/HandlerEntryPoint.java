package com.theplatform.dfh.cp.handler.executor.impl;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContextFactory;
import com.theplatform.dfh.cp.handler.executor.impl.processor.BaseAgendaProcessor;
import com.theplatform.dfh.cp.handler.executor.impl.processor.parallel.ParallelOperationAgendaProcessor;
import com.theplatform.dfh.cp.handler.kubernetes.support.config.KubernetesLaunchDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerEntryPoint extends BaseHandlerEntryPoint<ExecutorContext, BaseAgendaProcessor, KubernetesLaunchDataWrapper>
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
        //logger.debug(System.getProperty("user.dir"));
        logger.info(String.join("\n", args));
        new HandlerEntryPoint(args).execute();
        //ThreadUtils.logAliveThreads(6000L);
        logger.info("ExecutorComplete");
    }

    @Override
    protected KubernetesLaunchDataWrapper createLaunchDataWrapper(String[] args)
    {
        return new KubernetesLaunchDataWrapper(args);
    }

    @Override
    protected BaseOperationContextFactory<ExecutorContext> createOperationContextFactory(KubernetesLaunchDataWrapper launchDataWrapper)
    {
        return new ExecutorContextFactory(launchDataWrapper);
    }

    @Override
    protected BaseAgendaProcessor createHandlerProcessor(ExecutorContext executorContext)
    {
        //return new SequentialAgendaProcessor(launchDataWrapper, executorContext);
        // TODO: when ready we'll switch over (right now the sequence would need to use the yet-to-be-implemented dependsOn functionality)
        return new ParallelOperationAgendaProcessor(executorContext);
    }
}
