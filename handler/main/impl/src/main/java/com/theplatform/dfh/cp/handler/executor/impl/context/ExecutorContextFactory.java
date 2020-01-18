package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.handler.base.reporter.LogReporter;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.local.LocalOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.resident.ResidentOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.progress.agenda.ResourcePoolServiceAgendaProgressReporter;
import com.theplatform.dfh.cp.handler.executor.impl.properties.ExecutorProperty;
import com.theplatform.dfh.cp.handler.executor.impl.shutdown.KubernetesShutdownProcessor;
import com.theplatform.dfh.cp.handler.executor.impl.shutdown.ShutdownProcessor;
import com.theplatform.dfh.cp.handler.kubernetes.support.context.KubernetesOperationContextFactory;
import com.theplatform.dfh.cp.handler.util.http.impl.exception.HttpRequestHandlerException;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClientFactory;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.idm.IDMHTTPClientConfig;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
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

    public static final String IDM_URL_FIELD = "idm.url";
    public static final String IDM_USER = "idm.service.user.name";
    public static final String IDM_ENCRYPTED_PASS = "idm.service.user.encryptedpass";
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
                if(launchDataWrapper.getPropertyRetriever().getBoolean(ExecutorProperty.EXECUTOR_REAP_SELF, true))
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

        return new ExecutorContext(createAgendaReporter(urlConnectionFactory), launchDataWrapper, operationExecutorFactory, shutdownProcessors)
            .setUrlConnectionFactory(urlConnectionFactory);
    }

    @Override
    public ProgressReporter<OperationProgress> createReporter()
    {
        throw new UnsupportedOperationException();
    }

    public ProgressReporter<AgendaProgress> createAgendaReporter(HttpURLConnectionFactory httpURLConnectionFactory)
    {
        switch(getLaunchType())
        {
            case local:
            case docker:
                return new LogReporter<>();
            case kubernetes:
            default:
                return createHttpAgendaReporter(httpURLConnectionFactory);
        }
    }

    protected ProgressReporter<AgendaProgress> createHttpAgendaReporter(HttpURLConnectionFactory httpURLConnectionFactory)
    {
        FieldRetriever propertyRetriever = launchDataWrapper.getPropertyRetriever();

        String agendaProgressUrl = propertyRetriever.getField(AGENDA_PROGRESS_URL);
        logger.debug("AgendaProgressUrl: [" + agendaProgressUrl + "]");
        if(StringUtils.isBlank(agendaProgressUrl))
        {
            throw new RuntimeException("Invalid AgendaProgress url specified.");
        }
        return new ResourcePoolServiceAgendaProgressReporter(
            resourcePoolServiceClientFactory.create(agendaProgressUrl, httpURLConnectionFactory));
    }

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

    protected IDMHTTPUrlConnectionFactory createIDMHTTPUrlConnectionFactory(FieldRetriever propertyRetriever)
    {
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
    }

    public void setResourcePoolServiceClientFactory(ResourcePoolServiceClientFactory resourcePoolServiceClientFactory)
    {
        this.resourcePoolServiceClientFactory = resourcePoolServiceClientFactory;
    }
}
