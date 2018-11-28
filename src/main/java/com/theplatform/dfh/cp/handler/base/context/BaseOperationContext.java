package com.theplatform.dfh.cp.handler.base.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;

public abstract class BaseOperationContext
{
    private LaunchDataWrapper launchDataWrapper;

    @Deprecated
    public BaseOperationContext(Reporter reporter, LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

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
