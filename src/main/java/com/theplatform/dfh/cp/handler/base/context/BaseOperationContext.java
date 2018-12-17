package com.theplatform.dfh.cp.handler.base.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

public abstract class BaseOperationContext<T extends LaunchDataWrapper>
{
    private T launchDataWrapper;

    public BaseOperationContext(T launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void init(){}
    public void shutdown(){}

    public T getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(T launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }
}
