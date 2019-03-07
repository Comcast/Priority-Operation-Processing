package com.theplatform.dfh.cp.handler.base.processor;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.field.api.args.MetaData;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

public abstract class AbstractBaseHandlerProcessor<C extends BaseOperationContext> implements HandlerProcessor, MetaData<Object>
{

    protected  LaunchDataWrapper launchDataWrapper;
    protected C operationContext;

    public AbstractBaseHandlerProcessor(C operationContext)
    {
        this.launchDataWrapper = operationContext.getLaunchDataWrapper();
        this.operationContext = operationContext;
    }
}
