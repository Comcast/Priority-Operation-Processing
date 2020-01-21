package com.theplatform.dfh.cp.handler.executor.impl.processor.runner.event;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.progress.OperationProgress;

public interface OperationCompleteEvent
{
    void onOperationComplete(Operation operation, OperationProgress operationProgress);
}
