package com.theplatform.dfh.cp.handler.sample.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.reporter.progress.operation.OperationProgressFactory;
import com.theplatform.dfh.cp.handler.reporter.progress.operation.OperationProgressReporter;
import com.theplatform.dfh.cp.handler.reporter.progress.operation.OperationProgressThread;
import com.theplatform.dfh.cp.handler.reporter.progress.operation.OperationProgressThreadConfig;

public class OperationContext extends BaseOperationContext
{
    private OperationProgressReporter operationProgressReporter;
    private OperationProgressThread operationProgressThread;

    public OperationContext(Reporter reporter, LaunchDataWrapper launchDataWrapper)
    {
        super(reporter, launchDataWrapper);
        operationProgressThread = new OperationProgressThread(
            new OperationProgressThreadConfig()
                .setReporter(reporter)
        );
        this.operationProgressReporter = new OperationProgressReporter(operationProgressThread, new OperationProgressFactory());
    }

    public void init()
    {
        operationProgressThread.init();
    }

    public OperationProgressReporter getOperationProgressReporter()
    {
        return operationProgressReporter;
    }

    public void setOperationProgressReporter(OperationProgressReporter operationProgressReporter)
    {
        this.operationProgressReporter = operationProgressReporter;
    }

    public void shutdown()
    {
        operationProgressThread.shutdown(false);
    }
}
