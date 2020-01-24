package com.theplatform.dfh.cp.handler.executor.impl.progress.loader;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProgressLoader
{
    private static Logger logger = LoggerFactory.getLogger(FileProgressLoader.class);
    private ExecutorContext executorContext;

    public ProgressLoader(ExecutorContext executorContext)
    {
        this.executorContext = executorContext;
    }

    public abstract AgendaProgress loadProgress();

    protected AgendaProgress parseAgendaProgress(String agendaProgressJson)
    {
        if(agendaProgressJson == null)
            return null;

        try
        {
            return executorContext.getJsonHelper().getObjectFromString(agendaProgressJson, AgendaProgress.class);
        }
        catch(JsonHelperException e)
        {
            logger.error("Error reading string as AgendaProgress", e);
        }
        return null;
    }

    public ExecutorContext getExecutorContext()
    {
        return executorContext;
    }

    public void setExecutorContext(ExecutorContext executorContext)
    {
        this.executorContext = executorContext;
    }
}
