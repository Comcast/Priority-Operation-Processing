package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClient;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.DefaultAgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContextFactory;
import com.theplatform.dfh.cp.handler.puller.impl.processor.PullerProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullerEntryPoint extends BaseHandlerEntryPoint<PullerContext, PullerProcessor>
{
    private static Logger logger = LoggerFactory.getLogger(PullerEntryPoint.class);
    private AgendaClient agendaClient;

    public PullerEntryPoint(String[] args)
    {
        super(args);
        agendaClient = new DefaultAgendaClientFactory().getClient();
    }

    /**
     * Debugging/running with a local-only build use these args:
     * -launchType local -externalLaunchType local -propFile ./handler/main/package/local/config/external.properties
     *
     * Debugging running local with minikube for pod launches
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external-minikube.properties
     *
     * Debugging/running local with kubernetes for pod launches: (requires environment vars for k8s auth)
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external.properties
     *
     * @param args command line args
     */
    public static void main(String[] args)
    {
        logger.info(String.join("\n", args));
        new PullerEntryPoint(args).execute();

    }

    @Override
    protected LaunchDataWrapper createLaunchDataWrapper(String[] args)
    {
        return new DefaultLaunchDataWrapper(args);
    }

    @Override
    protected BaseOperationContextFactory<PullerContext> createOperationContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        return new PullerContextFactory(launchDataWrapper);
    }

    @Override
    protected PullerProcessor createHandlerProcessor(LaunchDataWrapper launchDataWrapper, PullerContext handlerContext)
    {
        return new PullerProcessor(launchDataWrapper, handlerContext, agendaClient);
    }
}
