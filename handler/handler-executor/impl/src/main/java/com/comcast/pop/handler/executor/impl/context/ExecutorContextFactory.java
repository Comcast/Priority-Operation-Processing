package com.comcast.pop.handler.executor.impl.context;

import com.comcast.pop.handler.executor.impl.exception.AgendaExecutorException;
import com.comcast.pop.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutorFactory;
import com.comcast.pop.handler.executor.impl.executor.local.LocalOperationExecutorFactory;
import com.comcast.pop.handler.executor.impl.executor.resident.ResidentOperationExecutorFactory;
import com.comcast.pop.handler.executor.impl.properties.ExecutorProperty;
import com.comcast.pop.handler.executor.impl.resident.generator.UpdateAgendaResidentHandlerFactory;
import com.comcast.pop.handler.executor.impl.shutdown.KubernetesShutdownProcessor;
import com.comcast.pop.handler.executor.impl.shutdown.ShutdownProcessor;
import com.comcast.pop.api.progress.OperationProgress;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comast.pop.handler.base.reporter.LogReporter;
import com.comast.pop.handler.base.reporter.ProgressReporter;
import com.comcast.pop.handler.executor.impl.executor.OperationExecutorFactory;
import com.comcast.pop.handler.executor.impl.progress.agenda.ResourcePoolServiceAgendaProgressReporter;
import com.comcast.pop.handler.kubernetes.support.context.KubernetesOperationContextFactory;
import com.comcast.pop.endpoint.client.ResourcePoolServiceClient;
import com.comcast.pop.endpoint.client.ResourcePoolServiceClientFactory;
import com.comcast.pop.http.api.AuthHttpURLConnectionFactory;
import com.comcast.pop.http.api.HttpURLConnectionFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Factory that creates a context object for this operation. This allows the command line to override the type of executor to use.
 */
public class ExecutorContextFactory extends KubernetesOperationContextFactory<ExecutorContext>
{
    private static Logger logger = LoggerFactory.getLogger(ExecutorContextFactory.class);

    public static final String AGENDA_PROGRESS_CONNECTION_TIMEOUT = "agenda.progress.connection.timeout";
    public static final String AGENDA_PROGRESS_URL = "agenda.progress.url";
    public static final String AGENDA_PROGRESS_PROXY_HOST = "agenda.progress.proxy.host";
    public static final String AGENDA_PROGRESS_PROXY_PORT = "agenda.progress.proxy.port";
    public static final int DEFAULT_AGENDA_PROGRESS_CONNECTION_TIMEOUT = 30000;

    private ResourcePoolServiceClientFactory resourcePoolServiceClientFactory;

    public ExecutorContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
        this.resourcePoolServiceClientFactory = new ResourcePoolServiceClientFactory();
    }

    @Override
    public ExecutorContext createOperationContext()
    {
        OperationExecutorFactory operationExecutorFactory;
        List<ShutdownProcessor> shutdownProcessors = new LinkedList<>();

        HttpURLConnectionFactory urlConnectionFactory = createIDMHTTPUrlConnectionFactory(launchDataWrapper.getPropertyRetriever());

        switch(getLaunchType())
        {
            case kubernetes:
                // self-reap is enabled by default
                if(launchDataWrapper.getPropertyRetriever().getBoolean(ExecutorProperty.REAP_SELF, true))
                    shutdownProcessors.add(new KubernetesShutdownProcessor(getKubeConfigFactory(), launchDataWrapper.getEnvironmentRetriever()));
                break;
        }

        switch (getExternalLaunchType())
        {
            case local:
                operationExecutorFactory = new LocalOperationExecutorFactory();
                break;
            case docker:
                // TODO: decide if we want to support docker ops execution...
                throw new AgendaExecutorException("Docker is not supported for agenda execution.");
            case kubernetes:
            default:
                operationExecutorFactory = new KubernetesOperationExecutorFactory(launchDataWrapper)
                    .setKubeConfigFactory(getKubeConfigFactory());
                break;
        }

        operationExecutorFactory.setResidentOperationExecutorFactory(new ResidentOperationExecutorFactory());

        ExecutorContext executorContext = new ExecutorContext(launchDataWrapper, operationExecutorFactory, shutdownProcessors)
            .setUrlConnectionFactory(urlConnectionFactory);

        setupResidentOperationExecutorFactory(operationExecutorFactory);
        configureLaunchTypeReporter(executorContext, urlConnectionFactory);
        return executorContext;
    }

    @Override
    public ProgressReporter<OperationProgress> createReporter()
    {
        throw new UnsupportedOperationException();
    }

    protected void setupResidentOperationExecutorFactory(OperationExecutorFactory operationExecutorFactory)
    {
        operationExecutorFactory.getResidentOperationExecutorFactory()
            .getResidentOperationsRegistry().registerHandlerFactory("updateAgenda", new UpdateAgendaResidentHandlerFactory());
    }

    public void configureLaunchTypeReporter(ExecutorContext executorContext, HttpURLConnectionFactory httpURLConnectionFactory)
    {
        switch(getLaunchType())
        {
            case local:
            case docker:
                executorContext.setReporter(new LogReporter<>());
                break;
            case kubernetes:
            default:
                setupHttpResourcePoolAccess(executorContext, httpURLConnectionFactory);

        }
    }

    protected void setupHttpResourcePoolAccess(ExecutorContext executorContext, HttpURLConnectionFactory httpURLConnectionFactory)
    {
        FieldRetriever propertyRetriever = launchDataWrapper.getPropertyRetriever();

        String agendaProgressUrl = propertyRetriever.getField(AGENDA_PROGRESS_URL);
        logger.debug("AgendaProgressUrl: [" + agendaProgressUrl + "]");
        if(StringUtils.isBlank(agendaProgressUrl))
        {
            throw new RuntimeException("Invalid AgendaProgress url specified.");
        }
        ResourcePoolServiceClient resourcePoolServiceClient =
            resourcePoolServiceClientFactory.create(agendaProgressUrl, httpURLConnectionFactory);
        executorContext.setResourcePoolServiceClient(resourcePoolServiceClient);
        executorContext.setReporter(new ResourcePoolServiceAgendaProgressReporter(resourcePoolServiceClient));
    }

    //TODO: restore the timeouts (the http stuff changed affecting its use)
    protected int getProgressTimeout(PropertyRetriever propertyRetriever)
    {
        String progressConnectionTimeoutString = propertyRetriever.getField(AGENDA_PROGRESS_CONNECTION_TIMEOUT, Integer.toString(DEFAULT_AGENDA_PROGRESS_CONNECTION_TIMEOUT));
        try
        {
            return Integer.parseInt(progressConnectionTimeoutString);
        }
        catch (NumberFormatException e)
        {
            logger.warn(String.format("Defaulting the invalid %1$s value.", AGENDA_PROGRESS_CONNECTION_TIMEOUT), e);
            return DEFAULT_AGENDA_PROGRESS_CONNECTION_TIMEOUT;
        }
    }

    protected HttpURLConnectionFactory createIDMHTTPUrlConnectionFactory(FieldRetriever propertyRetriever)
    {
        /** TODO: config use on the no auth factory would be nice...
        IDMHTTPClientConfig httpConfig = new IDMHTTPClientConfig();
        httpConfig.setIdentityUrl(propertyRetriever.getField(IDM_URL_FIELD));
        httpConfig.setUsername(propertyRetriever.getField(IDM_USER));
        httpConfig.setEncryptedPassword(propertyRetriever.getField(IDM_ENCRYPTED_PASS));
        if(httpConfig.getIdentityUrl() == null || httpConfig.getUsername() == null || httpConfig.getEncryptedPassword() == null)
        {
            throw new HttpRequestHandlerException("Invalid IDM credentials configured for token generation.");
        }

        httpConfig.setProxyHost(propertyRetriever.getField(AGENDA_PROGRESS_PROXY_HOST));
        httpConfig.setProxyPort(propertyRetriever.getField(AGENDA_PROGRESS_PROXY_PORT));
        logger.debug("Proxy=[" + httpConfig.getProxyHost() + ":" + httpConfig.getProxyPort() + "]");

        return new IDMHTTPUrlConnectionFactory(httpConfig)
            .setCid(retrieveCID());
         */
        return new AuthHttpURLConnectionFactory();
    }

    public void setResourcePoolServiceClientFactory(ResourcePoolServiceClientFactory resourcePoolServiceClientFactory)
    {
        this.resourcePoolServiceClientFactory = resourcePoolServiceClientFactory;
    }
}
