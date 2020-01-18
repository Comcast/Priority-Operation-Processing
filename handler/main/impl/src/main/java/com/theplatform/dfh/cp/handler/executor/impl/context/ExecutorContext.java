package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.api.LaunchType;
import com.theplatform.dfh.cp.handler.base.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.progress.agenda.AgendaProgressFactory;
import com.theplatform.dfh.cp.handler.executor.impl.progress.agenda.AgendaProgressReporter;
import com.theplatform.dfh.cp.handler.executor.impl.progress.agenda.AgendaProgressThread;
import com.theplatform.dfh.cp.handler.executor.impl.progress.agenda.AgendaProgressThreadConfig;
import com.theplatform.dfh.cp.handler.executor.impl.shutdown.ShutdownProcessor;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import org.apache.commons.lang.StringUtils;
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
    private static Logger logger = LoggerFactory.getLogger(ExecutorContext.class);
    private HttpURLConnectionFactory urlConnectionFactory;
    private OperationExecutorFactory operationExecutorFactory;
    private JsonHelper jsonHelper = new JsonHelper();
    private JsonContext jsonContext;
    private ProgressReporter reporter;
    private AgendaProgressReporter agendaProgressReporter;
    private AgendaProgressThread agendaProgressThread;
    private List<ShutdownProcessor> shutdownProcessors;
    private String agendaId;
    private String agendaProgressId;

    public ExecutorContext(ProgressReporter reporter, LaunchDataWrapper launchDataWrapper, OperationExecutorFactory operationExecutorFactory,
        List<ShutdownProcessor> shutdownProcessors)
    {
        super(launchDataWrapper);
        this.reporter = reporter;
        this.operationExecutorFactory = operationExecutorFactory;
        this.shutdownProcessors = new LinkedList<>();
        this.shutdownProcessors.addAll(shutdownProcessors);
        this.jsonContext = new JsonContext();
        agendaProgressThread = new AgendaProgressThread(createAgendaProgressThreadConfig(reporter));
        agendaProgressId = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.PROGRESS_ID.name(), null);
        if(agendaProgressId == null)
        {
            logger.warn("{} was unset, defaulting null", HandlerField.PROGRESS_ID.name());
        }
        agendaProgressReporter = new
            AgendaProgressReporter(agendaProgressThread, new AgendaProgressFactory(
            agendaProgressId
        ));
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

    protected void setAgendaProgressReporter(AgendaProgressReporter agendaProgressReporter)
    {
        this.agendaProgressReporter = agendaProgressReporter;
    }

    @Override
    public void init()
    {
        agendaProgressThread.init();
    }

    @Override
    public void shutdown()
    {
        agendaProgressThread.shutdown(false);
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
}
