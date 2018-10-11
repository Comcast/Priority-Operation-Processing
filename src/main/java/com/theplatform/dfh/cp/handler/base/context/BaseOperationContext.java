package com.theplatform.dfh.cp.handler.base.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;

public abstract class BaseOperationContext
{
    private Reporter reporter;
    private LaunchDataWrapper launchDataWrapper;

    public BaseOperationContext(Reporter reporter, LaunchDataWrapper launchDataWrapper)
    {
        this.reporter = reporter;
        this.launchDataWrapper = launchDataWrapper;
    }

    public void init(){}
    public void shutdown(){}

    public Reporter getReporter()
    {
        return reporter;
    }

    public void setReporter(Reporter reporter)
    {
        this.reporter = reporter;
    }

    public LaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }
}
