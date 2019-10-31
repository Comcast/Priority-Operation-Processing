package com.theplatform.dfh.cp.handler.base.progress.reporter.operation;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.progress.reporter.BaseReporterThreadConfig;
import com.theplatform.dfh.cp.handler.base.reporter.ProgressReporter;

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
