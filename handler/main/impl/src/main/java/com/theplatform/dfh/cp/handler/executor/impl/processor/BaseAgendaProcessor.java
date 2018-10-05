package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.progress.ProgressStatusUpdaterFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;

public abstract class BaseAgendaProcessor implements HandlerProcessor<Void>
{
    protected LaunchDataWrapper launchDataWrapper;
    protected ExecutorContext executorContext;
    protected JsonHelper jsonHelper;
    protected ProgressStatusUpdaterFactory progressStatusUpdaterFactory;

    public BaseAgendaProcessor(LaunchDataWrapper launchDataWrapper, ExecutorContext executorContext)
    {
        this(launchDataWrapper, executorContext, new ProgressStatusUpdaterFactory(launchDataWrapper));
    }

    public BaseAgendaProcessor(LaunchDataWrapper launchDataWrapper, ExecutorContext executorContext, ProgressStatusUpdaterFactory progressStatusUpdaterFactory)
    {
        this.launchDataWrapper = launchDataWrapper;
        this.executorContext = executorContext;
        this.progressStatusUpdaterFactory = progressStatusUpdaterFactory;
        this.jsonHelper = new JsonHelper();
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void setExecutorContext(ExecutorContext executorContext)
    {
        this.executorContext = executorContext;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
