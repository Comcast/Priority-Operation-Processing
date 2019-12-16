package com.theplatform.dfh.cp.handler.executor.impl.registry.resident.operations;

import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.resident.SampleResidentHandler;
import com.theplatform.dfh.cp.handler.filter.accelerate.impl.AccelerateFilterHandler;
import com.theplatform.dfh.cp.handler.filter.ldap.impl.LDAPFilterHandler;
import com.theplatform.dfh.cp.handler.util.http.impl.AgendaPostHandler;
import com.theplatform.dfh.cp.handler.util.http.impl.CreateAgendaHandler;
import com.theplatform.dfh.cp.handler.util.http.impl.IdmHttpRequestHandler;
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

    private static Map<String, Class<? extends ResidentHandler>> residentHandlerMap = new HashMap<>();

    static
    {
        residentHandlerMap.put("residentSample", SampleResidentHandler.class);
        residentHandlerMap.put("agendaPost", CreateAgendaHandler.class);
        residentHandlerMap.put("ldap", LDAPFilterHandler.class);
        residentHandlerMap.put("accelerate", AccelerateFilterHandler.class);
        residentHandlerMap.put("idmhttp", IdmHttpRequestHandler.class);
    }

    public ResidentHandler getHandler(String type)
    {
        Class<? extends ResidentHandler> residentHandlerClass = residentHandlerMap.get(type);
        if(residentHandlerClass != null)
        {
            try
            {
                return residentHandlerClass.newInstance();
            }
            catch(InstantiationException | IllegalAccessException e)
            {
                logger.error("Failed to instantiate ResidentHandler: {}", residentHandlerClass.getSimpleName());
            }
        }
        return null;
    }
}
