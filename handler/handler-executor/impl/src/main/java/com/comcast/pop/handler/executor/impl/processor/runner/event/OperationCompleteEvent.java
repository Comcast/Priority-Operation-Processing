package com.comcast.pop.handler.executor.impl.processor.runner.event;

import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.OperationProgress;

public interface OperationCompleteEvent
{
    void onOperationComplete(Operation operation, OperationProgress operationProgress);
}
