package com.comast.pop.handler.base.processor;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.context.BaseOperationContext;
import com.comast.pop.handler.base.field.api.args.MetaData;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBaseHandlerProcessor<L extends LaunchDataWrapper, C extends BaseOperationContext<L> > implements HandlerProcessor, MetaData<Object>
{
    protected  L launchDataWrapper;
    protected  C operationContext;
    protected Map<String,Object> handlerMetadata = new HashMap<>();


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


    /**
     * Not all executors of a subclass will need to pass along metadata, so they are checked.
     * @param executor
     */
    protected void assignHandlerMetadata(Object executor)
    {
        if(!(executor instanceof MetaData))
        {
            return;
        }
        MetaData<Object> metaData = (MetaData) executor;
        if(!metaData.getMetadata().isEmpty())
        {
            handlerMetadata.putAll(metaData.getMetadata());
        }
    }

    @Override
    public Map<String, Object> getMetadata()
    {
        return handlerMetadata;
    }
}
