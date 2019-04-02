package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.AbstractBaseHandlerProcessor;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseAgendaProcessor extends AbstractBaseHandlerProcessor<LaunchDataWrapper, ExecutorContext>
{
    private static Logger logger = LoggerFactory.getLogger(BaseAgendaProcessor.class);
    protected final long startTimeMillis;
    protected JsonHelper jsonHelper;
    private static final String DURATION_TEMPLATE = "Agenda %s (Ops) execution duration (milliseconds): %d.";

    public BaseAgendaProcessor(ExecutorContext executorContext)
    {
        super(executorContext);
        this.jsonHelper = new JsonHelper();
        extractAgendaMetadata();
        startTimeMillis = System.currentTimeMillis();
    }

    private void extractAgendaMetadata()
    {
        String agendaId = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.AGENDA_ID.name());
        if(agendaId != null)
        {
            getMetadata().put(HandlerField.AGENDA_ID.name(), agendaId);
        }
    }

    /**
     * Executes the Agenda processing
     */
    @Override
    public void execute()
    {
        try
        {
            doExecute();
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            logDuration();
        }
    }

    protected abstract void doExecute();

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void setExecutorContext(ExecutorContext executorContext)
    {
        this.operationContext = executorContext;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    protected void logDuration()
    {
        String key = HandlerField.AGENDA_ID.name();
        String agendaId = getMetadata().keySet().contains(key)? (String) getMetadata().get(key) : "no_agenda_id";
        logger.info(String.format(DURATION_TEMPLATE, agendaId, System.currentTimeMillis() - startTimeMillis));
    }
}
