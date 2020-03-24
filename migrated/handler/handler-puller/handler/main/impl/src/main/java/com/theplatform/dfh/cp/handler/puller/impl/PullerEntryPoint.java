package com.theplatform.dfh.cp.handler.puller.impl;

import com.theplatform.dfh.cp.handler.base.BaseHandlerEntryPoint;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.base.messages.HandlerMessages;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerConfig;
import com.theplatform.dfh.cp.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContext;
import com.theplatform.dfh.cp.handler.puller.impl.context.PullerContextFactory;
import com.theplatform.dfh.cp.handler.puller.impl.processor.PullerProcessor;
import com.theplatform.dfh.cp.handler.puller.impl.retriever.PullerArgumentProvider;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PullerEntryPoint extends BaseHandlerEntryPoint<PullerContext, PullerProcessor, PullerLaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(PullerEntryPoint.class);
    private static final String DEFAULT_CONF_PATH = "/app/config/conf.yaml";

    private MetricReporter metricReporter;

    public PullerEntryPoint(String[] args)
    {
        super(args);
        logger.debug("ARGS: {}", String.join("\n", args));
    }

    @Override
    protected PullerLaunchDataWrapper createLaunchDataWrapper(String[] args)
    {
        return new PullerLaunchDataWrapper(new ArgumentRetriever(new PullerArgumentProvider(args)));
    }

    @Override
    protected BaseOperationContextFactory<PullerContext> createOperationContextFactory(PullerLaunchDataWrapper launchDataWrapper)
    {
        return new PullerContextFactory(launchDataWrapper);
    }

    @Override
    protected PullerProcessor createHandlerProcessor(PullerContext handlerContext)
    {
        return new PullerProcessor(handlerContext);
    }

    /**
     * Be sure to specify -Dlogback.configurationFile=./handler/main/package/monitors/logback_local.xml (VM option)
     *
     * Debugging/running with a local-only build use these args:
     * -launchType local -externalLaunchType local -propFile ./handler/main/package/local/config/external.properties -confPath ./handler/main/package/local/config/conf.yaml
     *
     * Debugging running local with minikube for pod launches
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/main/package/local/config/external-minikube.properties -confPath ./handler/main/package/local/config/conf.yaml
     *
     * NOTE! For kubernetes external execution use -oauthCertPath [full file path] -oauthTokenPath [full file path]
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
        String confPath = argumentRetriever.getField(PullerArgumentProvider.CONF_PATH, DEFAULT_CONF_PATH);
        String[] args2 = new String[] {"server", confPath};
        new PullerApp(pullerEntryPoint).run(args2);
    }

    /**
     * Simplified execute method specific to the puller.
     */
    @Override
    public void execute()
    {
        // get the operation specific context for running the overall process
        PullerContext operationContext = getOperationContextFactory().createOperationContext();
        try
        {
            operationContext.init();
            PullerProcessor pullerProcessor = createHandlerProcessor(operationContext);
            pullerProcessor.setResourceCheckers(operationContext.getResourceCheckerFactory().getResourceCheckers());
            pullerProcessor.setMetricReporter(metricReporter);
            pullerProcessor.execute();
        }
        catch (Exception e)
        {
            operationContext.processUnhandledException(HandlerMessages.GENERAL_HANDLER_ERROR.getMessage("Puller"), e);
        }
        finally
        {
            operationContext.shutdown();
        }
    }

    public PullerConfig getPullerConfig()
    {
        return getLaunchDataWrapper().getPullerConfig();
    }

    public PullerEntryPoint setPullerConfig(PullerConfig pullerConfig)
    {
        getLaunchDataWrapper().setPullerConfig(pullerConfig);
        return this;
    }
    public void setMetricReporter(MetricReporter metricReporter)
    {
        if(metricReporter != null)
        {
            this.metricReporter = metricReporter;
        }
    }
}
