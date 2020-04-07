package com.comcast.pop.handler.executor.impl.resident.generator;

import com.comast.pop.handler.base.ResidentHandler;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.handler.executor.impl.registry.resident.operations.ResidentHandlerFactory;

public class UpdateAgendaResidentHandlerFactory implements ResidentHandlerFactory
{
    @Override
    public ResidentHandler create(ExecutorContext executorContext)
    {
        return new UpdateAgendaResidentHandler(executorContext);
    }
}
