package com.comcast.fission.handler.executor.impl.registry.resident.operations;

import com.comcast.fission.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;

public interface ResidentHandlerFactory
{
    ResidentHandler create(ExecutorContext executorContext);
}
