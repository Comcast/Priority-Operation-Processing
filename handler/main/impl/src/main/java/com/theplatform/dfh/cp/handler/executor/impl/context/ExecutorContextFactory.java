package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.local.LocalOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.executor.resident.ResidentOperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.properties.ExecutorProperty;
import com.theplatform.dfh.cp.handler.executor.impl.shutdown.KubernetesShutdownProcessor;
import com.theplatform.dfh.cp.handler.executor.impl.shutdown.ShutdownProcessor;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.kubernetes.support.context.KubernetesOperationContextFactory;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.log.LogReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.HttpReporter;
import com.theplatform.dfh.cp.handler.util.http.impl.exception.HttpRequestHandlerException;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import com.theplatform.module.authentication.client.EncryptedAuthenticationClient;
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
    public static final int DEFAULT_AGENDA_PROGRESS_CONNECTION_TIMEOUT = 30000;

    public ExecutorContextFactory(LaunchDataWrapper launchDataWrapper)
    {
        super(launchDataWrapper);
    }

    @Override
    public ExecutorContext createOperationContext()
    {
        OperationExecutorFactory operationExecutorFactory;
        List<ShutdownProcessor> shutdownProcessors = new LinkedList<>();

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

        return new ExecutorContext(createReporter(), launchDataWrapper, operationExecutorFactory, shutdownProcessors);
    }

    @Override
    public ProgressReporter createReporter()
    {
        switch(getLaunchType())
        {
            case local:
            case docker:
                return new LogReporter();
            case kubernetes:
            default:
                return createHttpReporter();
        }
    }

    protected HttpReporter createHttpReporter()
    {
        FieldRetriever propertyRetriever = launchDataWrapper.getPropertyRetriever();
        FieldRetriever environmentRetriever = launchDataWrapper.getEnvironmentRetriever();

        int progressConnectionTimeout;
        String agendaProgressUrl = propertyRetriever.getField(AGENDA_PROGRESS_URL);
        String progressConnectionTimeoutString = propertyRetriever.getField(AGENDA_PROGRESS_CONNECTION_TIMEOUT, Integer.toString(DEFAULT_AGENDA_PROGRESS_CONNECTION_TIMEOUT));
        try
        {
            progressConnectionTimeout = Integer.parseInt(progressConnectionTimeoutString);
        }
        catch (NumberFormatException e)
        {
            progressConnectionTimeout = DEFAULT_AGENDA_PROGRESS_CONNECTION_TIMEOUT;
            logger.warn(String.format("Defaulting the invalid %1$s value.", AGENDA_PROGRESS_CONNECTION_TIMEOUT), e);
        }

        if(StringUtils.isBlank(agendaProgressUrl))
        {
            throw new RuntimeException("Invalid AgendaProgress url specified.");
        }

        return new HttpReporter()
            .setUrlRequestPerformer(new URLRequestPerformer())
            .setHttpURLConnectionFactory(createIDMHTTPUrlConnectionFactory(propertyRetriever, environmentRetriever))
            .setReportingUrl(agendaProgressUrl)
            .setConnectionTimeoutMilliseconds(progressConnectionTimeout);
    }

    protected static IDMHTTPUrlConnectionFactory createIDMHTTPUrlConnectionFactory(FieldRetriever propertyRetriever, FieldRetriever environmentRetriever)
    {
        String identityUrl = propertyRetriever.getField(IDM_URL_FIELD);
        String user = propertyRetriever.getField(IDM_USER);
        String encryptedPass = propertyRetriever.getField(IDM_ENCRYPTED_PASS);
        if(identityUrl == null || user == null || encryptedPass == null)
        {
            throw new HttpRequestHandlerException("Invalid IDM credentials configured for token generation.");
        }

        IDMHTTPUrlConnectionFactory connectionFactory = new IDMHTTPUrlConnectionFactory(new EncryptedAuthenticationClient(
            identityUrl,
            user,
            encryptedPass,
            null
        ));
        connectionFactory.setCid(environmentRetriever.getField(HandlerField.CID.name(), null));

        return connectionFactory;
    }
}
