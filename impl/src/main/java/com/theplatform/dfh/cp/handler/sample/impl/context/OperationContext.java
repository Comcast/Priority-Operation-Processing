package com.theplatform.dfh.cp.handler.sample.impl.context;

import com.theplatform.dfh.cp.handler.reporter.api.Reporter;

public class OperationContext
{
    private Reporter reporter;

    public OperationContext(Reporter reporter)
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
