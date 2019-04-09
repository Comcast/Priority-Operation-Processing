package com.theplatform.dfh.cp.handler.sample.impl.context;

import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.operation.OperationProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.operation.context.ProgressOperationContext;

public class OperationContext extends ProgressOperationContext<LaunchDataWrapper>
{
    private OperationProgressReporter operationProgressReporter;

    public OperationContext(ProgressReporter reporter, LaunchDataWrapper launchDataWrapper)
    {
        super(reporter, launchDataWrapper);
    }

    public OperationProgressReporter getOperationProgressReporter()
    {
        return operationProgressReporter;
    }

    public void setOperationProgressReporter(OperationProgressReporter operationProgressReporter)
    {
        this.operationProgressReporter = operationProgressReporter;
    }
}
