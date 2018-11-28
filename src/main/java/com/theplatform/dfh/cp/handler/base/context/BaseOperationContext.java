package com.theplatform.dfh.cp.handler.base.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;

public abstract class BaseOperationContext
{
    private LaunchDataWrapper launchDataWrapper;

    public BaseOperationContext(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void init(){}
    public void shutdown(){}

    public LaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }
}
