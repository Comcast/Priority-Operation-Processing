package com.comast.pop.handler.base.progress.reporter.operation;

import com.comcast.pop.api.progress.OperationProgress;

public interface OperationProgressConsumer
{
    void setOperationProgress(OperationProgress operationProgress, Object payload);
    void setOperationProgress(OperationProgress operationProgress);
}
