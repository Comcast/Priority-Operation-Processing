package com.theplatform.dfh.cp.handler.executor.impl.registry.resident.operations;

import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;

public interface ResidentHandlerFactory
{
    ResidentHandler create(ExecutorContext executorContext);
}
