package com.theplatform.dfh.cp.handler.base.progress.reporter.operation;

import com.theplatform.dfh.cp.api.progress.OperationProgress;

public interface OperationProgressConsumer
{
    void setOperationProgress(OperationProgress operationProgress, Object payload);
    void setOperationProgress(OperationProgress operationProgress);
}
