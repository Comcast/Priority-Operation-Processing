package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.executor.impl.shutdown.ShutdownProcessor;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.api.LaunchType;
import com.theplatform.dfh.cp.handler.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressFactory;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressThread;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressThreadConfig;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * The context for this instance of the Executor
 */
public class ExecutorContext extends BaseOperationContext<LaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(ExecutorContext.class);
    private OperationExecutorFactory operationExecutorFactory;
    private JsonContext jsonContext;
    private ProgressReporter reporter;
    private AgendaProgressReporter agendaProgressReporter;
    private AgendaProgressThread agendaProgressThread;
    private List<ShutdownProcessor> shutdownProcessors;

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
        String progressId = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.PROGRESS_ID.name(), null);
        if(progressId == null)
        {
            logger.warn("{} was unset, defaulting null", HandlerField.PROGRESS_ID.name());
        }
        agendaProgressReporter = new
            AgendaProgressReporter(agendaProgressThread, new AgendaProgressFactory(
            progressId
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
}
