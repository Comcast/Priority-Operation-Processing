package com.theplatform.dfh.cp.handler.base.processor;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.field.api.args.MetaData;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

public abstract class AbstractBaseHandlerProcessor<L extends LaunchDataWrapper, C extends BaseOperationContext<L> > implements HandlerProcessor, MetaData<Object>
{
    protected  L launchDataWrapper;
    protected  C operationContext;

    public AbstractBaseHandlerProcessor(C operationContext)
    {
        this.launchDataWrapper = operationContext.getLaunchDataWrapper();
        this.operationContext = operationContext;
    }

    public void setLaunchDataWrapper(L launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void setOperationContext(C operationContext)
    {
        this.operationContext = operationContext;
    }

    public L getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public C getOperationContext()
    {
        return operationContext;
    }
}
