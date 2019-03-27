package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.AbstractBaseHandlerProcessor;
import com.theplatform.dfh.cp.handler.base.processor.HandlerMetadata;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.field.api.args.MetaData;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseAgendaProcessor extends AbstractBaseHandlerProcessor<LaunchDataWrapper, ExecutorContext>
{
    protected JsonHelper jsonHelper;

    public BaseAgendaProcessor(ExecutorContext executorContext)
    {
        super(executorContext);
        this.jsonHelper = new JsonHelper();
    }

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
}
