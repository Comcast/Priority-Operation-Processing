package com.theplatform.dfh.cp.handler.base.processor;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.field.api.args.MetaData;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBaseHandlerProcessor<L extends LaunchDataWrapper, C extends BaseOperationContext<L> > implements HandlerProcessor, MetaData<Object>
{
    protected  L launchDataWrapper;
    protected  C operationContext;
    private Map<String,Object> handlerMetadata = new HashMap<>();


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
     * Not all subclasses need to pass along metadata, so is only called when needed.
     * @param metaData
     */
    protected void assignHandlerMetadata(MetaData metaData)
    {
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
