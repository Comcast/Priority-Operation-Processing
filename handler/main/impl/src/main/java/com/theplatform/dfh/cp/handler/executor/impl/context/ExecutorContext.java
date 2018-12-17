package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressFactory;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressThread;
import com.theplatform.dfh.cp.handler.reporter.progress.agenda.AgendaProgressThreadConfig;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * The context for this instance of the Executor
 */
public class ExecutorContext extends BaseOperationContext<LaunchDataWrapper>
{
    private static Logger logger = LoggerFactory.getLogger(ExecutorContext.class);
    private OperationExecutorFactory operationExecutorFactory;
    private JsonContext jsonContext;
    private Reporter reporter;
    private AgendaProgressReporter agendaProgressReporter;
    private AgendaProgressThread agendaProgressThread;

    public ExecutorContext(Reporter reporter, LaunchDataWrapper launchDataWrapper, OperationExecutorFactory operationExecutorFactory)
    {
        super(launchDataWrapper);
        this.reporter = reporter;
        this.operationExecutorFactory = operationExecutorFactory;
        this.jsonContext = new JsonContext();
        agendaProgressThread = new AgendaProgressThread(
            new AgendaProgressThreadConfig()
                .setReporter(reporter)
        );
        String progressId = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.PROGRESS_ID.name(), null);
        if(progressId == null)
        {
            progressId = UUID.randomUUID().toString();
            logger.warn("{} was unset, defaulting to generated: {}", HandlerField.PROGRESS_ID.name(), progressId);
        }
        agendaProgressReporter = new
            AgendaProgressReporter(agendaProgressThread, new AgendaProgressFactory(
            progressId
        ));
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

    public Reporter getReporter()
    {
        return reporter;
    }
}
