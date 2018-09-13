package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClient;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AgendaClientFactory;
import com.theplatform.dfh.cp.handler.puller.impl.client.agenda.AwsAgendaProviderClient;
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

    private AgendaClientFactory agendaClientFactory;

    public PullerEntryPoint(String[] args)
    {
        super(args);
        logger.info("****ARGS: {}", String.join("\n", args));

        agendaClientFactory = new DefaultAgendaClientFactory();
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
        return new PullerProcessor(launchDataWrapper, handlerContext, agendaClientFactory);
    }

    /**
     * Debugging/running with a local-only build use these args:
     * -launchType local -externalLaunchType local -propFile ./handler/main/package/local/config/external.properties -confPath ./handler/main/package/local/config/conf.yaml
     *
     * Debugging running local with minikube for pod launches
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external-minikube.properties -confPath ./handler/main/package/local/config/conf.yaml
     *
     * Debugging/running local with kubernetes for pod launches: (requires environment vars for k8s auth)
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external.properties -confPath ./handler/main/package/local/config/conf.yaml
     *
     * @param args command line args
     */
    public static void main( String[] args ) throws Exception
    {
        PullerEntryPoint pullerEntryPoint = new PullerEntryPoint(args);
        FieldRetriever argumentRetriever = pullerEntryPoint.getLaunchDataWrapper().getArgumentRetriever();
        String confPath = argumentRetriever.getField(PullerArgumentProvider.CONF_PATH, "/config/conf.yaml");
        String[] args2 = new String[] {"server", confPath};
        new PullerApp(pullerEntryPoint).run(args2);
    }

    public AgendaClientFactory getAgendaClientFactory()
    {
        return agendaClientFactory;
    }

    public PullerEntryPoint setAgendaClientFactory(AgendaClientFactory agendaClientFactory)
    {
        this.agendaClientFactory = agendaClientFactory;
        return this;
    }
}
