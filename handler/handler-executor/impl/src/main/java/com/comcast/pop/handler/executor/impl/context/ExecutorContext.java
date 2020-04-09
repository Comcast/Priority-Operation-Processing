package com.comcast.pop.handler.executor.impl.context;

import com.comast.pop.handler.base.payload.PayloadWriterFactory;
import com.comcast.pop.endpoint.client.ResourcePoolServiceClient;
import com.comcast.pop.handler.executor.impl.progress.agenda.AgendaProgressFactory;
import com.comcast.pop.handler.executor.impl.properties.ExecutorProperty;
import com.comcast.pop.handler.executor.impl.shutdown.ShutdownProcessor;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comast.pop.handler.base.context.BaseOperationContext;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.api.LaunchType;
import com.comast.pop.handler.base.field.api.args.HandlerArgument;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.reporter.ProgressReporter;
import com.comcast.pop.handler.executor.impl.executor.OperationExecutorFactory;
import com.comcast.pop.handler.executor.impl.progress.agenda.AgendaProgressReporter;
import com.comcast.pop.handler.executor.impl.progress.agenda.AgendaProgressThread;
import com.comcast.pop.handler.executor.impl.progress.agenda.AgendaProgressThreadConfig;
import com.comcast.pop.handler.kubernetes.support.payload.PayloadWriterFactoryImpl;
import com.comcast.pop.http.api.HttpURLConnectionFactory;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.modules.jsonhelper.replacement.JsonContext;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * The context for this instance of the Executor
 */
public class ExecutorContext extends BaseOperationContext<LaunchDataWrapper>
{
    private final static int DEFAULT_PROGRESS_THREAD_EXIT_TIMEOUT_MS = 60000;

    private static Logger logger = LoggerFactory.getLogger(ExecutorContext.class);

    private HttpURLConnectionFactory urlConnectionFactory;
    private OperationExecutorFactory operationExecutorFactory;
    private PayloadWriterFactory<ExecutionConfig> payloadWriterFactory;
    private ResourcePoolServiceClient resourcePoolServiceClient;
    private JsonHelper jsonHelper = new JsonHelper();
    private JsonContext jsonContext;
    private ProgressReporter reporter;
    private AgendaProgressReporter agendaProgressReporter;
    private AgendaProgressThread agendaProgressThread;
    private List<ShutdownProcessor> shutdownProcessors;
    private String agendaId;
    private String agendaProgressId;
    private Agenda agenda;

    public ExecutorContext(LaunchDataWrapper launchDataWrapper, OperationExecutorFactory operationExecutorFactory,
        List<ShutdownProcessor> shutdownProcessors)
    {
        super(launchDataWrapper);
        this.operationExecutorFactory = operationExecutorFactory;
        this.shutdownProcessors = new LinkedList<>();
        this.shutdownProcessors.addAll(shutdownProcessors);
        this.jsonContext = new JsonContext();
        payloadWriterFactory = new PayloadWriterFactoryImpl(launchDataWrapper);
    }

    private AgendaProgressThreadConfig createAgendaProgressThreadConfig(ProgressReporter reporter)
    {
        return new AgendaProgressThreadConfig()
            .setReporter(reporter)
            .setRequireProgressId(isKubernetesLaunched());
    }

    private boolean isKubernetesLaunched()
    {
        return StringUtils.equalsIgnoreCase(
            LaunchType.kubernetes.name(),
            getLaunchDataWrapper().getArgumentRetriever().getField(HandlerArgument.LAUNCH_TYPE.getArgumentName(), LaunchType.kubernetes.name())
        );
    }

    @Override
    public void processUnhandledException(String s, Exception e)
    {
        if(agendaProgressReporter != null)
            agendaProgressReporter.addFailed(new DiagnosticEvent(s, e));
    }

    public AgendaProgressReporter getAgendaProgressReporter()
    {
        return agendaProgressReporter;
    }

    @Override
    public void init()
    {
        agendaProgressThread = new AgendaProgressThread(createAgendaProgressThreadConfig(reporter));
        agendaProgressId = getLaunchDataWrapper().getEnvironmentRetriever().getField(HandlerField.PROGRESS_ID.name(), null);
        if(agendaProgressId == null)
        {
            logger.warn("{} was unset, defaulting null", HandlerField.PROGRESS_ID.name());
        }
        agendaProgressReporter = new
            AgendaProgressReporter(agendaProgressThread, new AgendaProgressFactory(
            agendaProgressId
        ));
        agendaProgressThread.init();
    }

    @Override
    public void shutdown()
    {
        agendaProgressThread.shutdown(false);
        try
        {
            // give the progress thread some time to report before giving up
            Thread progressReportThread = agendaProgressThread.getReporterThread();
            progressReportThread.join(
                getLaunchDataWrapper().getPropertyRetriever().getInt(ExecutorProperty.PROGRESS_THREAD_EXIT_TIMEOUT_MS, DEFAULT_PROGRESS_THREAD_EXIT_TIMEOUT_MS));
            if(progressReportThread.isAlive())
                logger.warn("AgendaProgressThread join timed out.");
        }
        catch(InterruptedException e)
        {
            logger.warn("Reporter Thread join call was interrupted.", e);
        }
        shutdownProcessors.forEach(ShutdownProcessor::shutdown);
    }

    public OperationExecutorFactory getOperationExecutorFactory()
    {
        return operationExecutorFactory;
    }

    public void setOperationExecutorFactory(OperationExecutorFactory operationExecutorFactory)
    {
        this.operationExecutorFactory = operationExecutorFactory;
    }

    public JsonContext getJsonContext()
    {
        return jsonContext;
    }

    public void setJsonContext(JsonContext jsonContext)
    {
        this.jsonContext = jsonContext;
    }

    public ProgressReporter getReporter()
    {
        return reporter;
    }

    public void setReporter(ProgressReporter reporter)
    {
        this.reporter = reporter;
    }

    public String getAgendaId()
    {
        return agendaId;
    }

    public ExecutorContext setAgendaId(String agendaId)
    {
        if(agendaId == null)
        {
            agendaId = UUID.randomUUID().toString();
            logger.warn(String.format("Id was not set on the incoming Agenda. Generated Id: %1$s", agendaId));
        }
        this.agendaId = agendaId;
        return this;
    }

    public String getAgendaProgressId()
    {
        return agendaProgressId;
    }

    public void setAgendaProgressId(String agendaProgressId)
    {
        this.agendaProgressId = agendaProgressId;
    }

    public void setAgenda(Agenda agenda)
    {
        this.agenda = agenda;
    }

    public Agenda getAgenda()
    {
        return agenda;
    }

    public HttpURLConnectionFactory getUrlConnectionFactory()
    {
        return urlConnectionFactory;
    }

    public ExecutorContext setUrlConnectionFactory(HttpURLConnectionFactory urlConnectionFactory)
    {
        this.urlConnectionFactory = urlConnectionFactory;
        return this;
    }

    public JsonHelper getJsonHelper()
    {
        return jsonHelper;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public PayloadWriterFactory<ExecutionConfig> getPayloadWriterFactory()
    {
        return payloadWriterFactory;
    }

    public void setPayloadWriterFactory(
        PayloadWriterFactory<ExecutionConfig> payloadWriterFactory)
    {
        this.payloadWriterFactory = payloadWriterFactory;
    }

    public ResourcePoolServiceClient getResourcePoolServiceClient()
    {
        return resourcePoolServiceClient;
    }

    public void setResourcePoolServiceClient(ResourcePoolServiceClient resourcePoolServiceClient)
    {
        this.resourcePoolServiceClient = resourcePoolServiceClient;
    }
}
