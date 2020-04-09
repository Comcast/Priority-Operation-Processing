package com.comcast.pop.handler.puller.impl;

import com.comcast.pop.handler.puller.impl.retriever.PullerArgumentProvider;
import com.comast.pop.handler.base.BaseHandlerEntryPoint;
import com.comast.pop.handler.base.context.BaseOperationContextFactory;
import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.comast.pop.handler.base.field.retriever.argument.ArgumentRetriever;
import com.comast.pop.handler.base.messages.HandlerMessages;
import com.comcast.pop.handler.puller.impl.config.PullerLaunchDataWrapper;
import com.comcast.pop.handler.puller.impl.context.PullerContext;
import com.comcast.pop.handler.puller.impl.context.PullerContextFactory;
import com.comcast.pop.handler.puller.impl.processor.PullerProcessor;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
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
     * -launchType local -externalLaunchType local -propFile ./handler/handler-puller/package/local/config/external.properties -confPath ./handler/handler-puller/package/local/config/conf.yaml
     *
     * Debugging running local with minikube for pod launches
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/handler-puller/package/local/config/external-minikube.properties -confPath ./handler/handler-puller/package/local/config/conf.yaml
     *
     * NOTE! For kubernetes external execution use -oauthCertPath [full file path] -oauthTokenPath [full file path]
     *
     * Debugging/running local with kubernetes for pod launches: (requires environment vars for k8s auth)
     * -launchType local -externalLaunchType kubernetes -propFile ./handler/handler-puller/package/local/config/external.properties -confPath ./handler/handler-puller/package/local/config/conf.yaml
     *
     * @param args command line args
     */
    public static void main( String[] args ) throws Exception
    {
        //logger.debug(System.getProperty("user.dir"));
        new PullerEntryPoint(args).execute();
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

    public void setMetricReporter(MetricReporter metricReporter)
    {
        if(metricReporter != null)
        {
            this.metricReporter = metricReporter;
        }
    }
}
