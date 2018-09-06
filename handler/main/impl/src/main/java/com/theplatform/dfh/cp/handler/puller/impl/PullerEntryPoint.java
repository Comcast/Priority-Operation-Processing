package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClient;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.DefaultAgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContextFactory;
import com.theplatform.dfh.cp.handler.puller.impl.processor.PullerProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.DefaultLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.retriever.PullerArgumentProvider;
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

    @Override
    protected LaunchDataWrapper createLaunchDataWrapper(String[] args)
    {
        return new DefaultLaunchDataWrapper(new ArgumentRetriever(new PullerArgumentProvider(args)));
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
