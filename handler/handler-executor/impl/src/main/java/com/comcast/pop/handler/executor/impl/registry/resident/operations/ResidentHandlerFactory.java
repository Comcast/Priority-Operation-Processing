package com.comcast.pop.handler.executor.impl.registry.resident.operations;

import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comast.pop.handler.base.ResidentHandler;

public interface ResidentHandlerFactory
{
    ResidentHandler create(ExecutorContext executorContext);
}
