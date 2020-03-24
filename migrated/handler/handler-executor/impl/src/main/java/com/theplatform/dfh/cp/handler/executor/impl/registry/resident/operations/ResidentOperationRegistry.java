package com.theplatform.dfh.cp.handler.executor.impl.registry.resident.operations;

import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.resident.SampleResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.resident.log.LogResidentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of ResidentHandlers (could use some plugin function if that seems like a necessity...)
 */
public class ResidentOperationRegistry
{
    private static Logger logger = LoggerFactory.getLogger(ResidentOperationRegistry.class);

    private Map<String, ResidentHandlerFactory> residentHandlerMap = new HashMap<>();

    public ResidentOperationRegistry()
    {
        registerDefaultHandlers();
    }

    private void registerDefaultHandlers()
    {
        // NOTE: this is only the simple resident handlers. Any that require additional functionality use registerHandlerFactory (op generator for example)
        residentHandlerMap.put("residentSample", new BasicResidentHandlerFactory<>(SampleResidentHandler.class));
        residentHandlerMap.put("logMessages", new BasicResidentHandlerFactory<>(LogResidentHandler.class));
// TODO: consider restoration
//      residentHandlerMap.put("agendaPost", new BasicResidentHandlerFactory<>(CreateAgendaHandler.class));
    }

    public ResidentHandler getHandler(ExecutorContext executorContext, String type)
    {
        ResidentHandlerFactory residentHandlerFactory = residentHandlerMap.get(type);
        if(residentHandlerFactory != null)
        {
            return residentHandlerFactory.create(executorContext);
        }
        return null;
    }

    public void registerHandlerFactory(String id, ResidentHandlerFactory residentHandlerFactory)
    {
        residentHandlerMap.put(id, residentHandlerFactory);
    }
}
