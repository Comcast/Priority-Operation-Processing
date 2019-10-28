package com.theplatform.dfh.cp.handler.base.progress.reporter.operation;

import com.theplatform.dfh.cp.handler.base.progress.reporter.BaseReporterThreadConfig;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;

/**
 * Configuration for the op reporter thread
 */
public class OperationProgressThreadConfig extends BaseReporterThreadConfig
{
    private ProgressReporter reporter;

    public ProgressReporter getReporter()
    {
        return reporter;
    }

    public OperationProgressThreadConfig setReporter(ProgressReporter reporter)
    {
        this.reporter = reporter;
        return this;
    }
}
