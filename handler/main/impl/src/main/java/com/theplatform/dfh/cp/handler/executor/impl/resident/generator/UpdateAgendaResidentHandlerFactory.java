package com.theplatform.dfh.cp.handler.executor.impl.resident.generator;

import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.registry.resident.operations.ResidentHandlerFactory;

public class UpdateAgendaResidentHandlerFactory implements ResidentHandlerFactory
{
    @Override
    public ResidentHandler create(ExecutorContext executorContext)
    {
        return new UpdateAgendaResidentHandler(executorContext);
    }
}
