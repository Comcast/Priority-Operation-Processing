package com.comast.pop.handler.base.progress.reporter.operation;

import com.comast.pop.handler.base.reporter.ProgressReporter;
import com.comcast.pop.api.progress.OperationProgress;
import com.comast.pop.handler.base.progress.reporter.BaseReporterThreadConfig;

/**
 * Configuration for the op reporter thread
 */
public class OperationProgressThreadConfig extends BaseReporterThreadConfig
{
    private ProgressReporter<OperationProgress> reporter;

    public ProgressReporter<OperationProgress> getReporter()
    {
        return reporter;
    }

    public OperationProgressThreadConfig setReporter(ProgressReporter<OperationProgress> reporter)
    {
        this.reporter = reporter;
        return this;
    }
}
