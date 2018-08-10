package com.theplatform.dfh.cp.handler.base.context;

import com.theplatform.dfh.cp.handler.reporter.api.Reporter;

public abstract class BaseOperationContext
{
    private Reporter reporter;

    public BaseOperationContext(Reporter reporter)
    {
        this.reporter = reporter;
    }

    public Reporter getReporter()
    {
        return reporter;
    }

    public void setReporter(Reporter reporter)
    {
        this.reporter = reporter;
    }
}
