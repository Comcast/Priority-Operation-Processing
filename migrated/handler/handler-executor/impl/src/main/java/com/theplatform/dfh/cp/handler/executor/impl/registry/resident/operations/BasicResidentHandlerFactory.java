package com.theplatform.dfh.cp.handler.executor.impl.registry.resident.operations;

import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicResidentHandlerFactory<T extends ResidentHandler> implements ResidentHandlerFactory
{
    private static Logger logger = LoggerFactory.getLogger(BasicResidentHandlerFactory.class);

    private Class<T> residentHandlerClass;

    public BasicResidentHandlerFactory(Class<T> residentHandlerClass)
    {
        this.residentHandlerClass = residentHandlerClass;
    }

    @Override
    public ResidentHandler create(ExecutorContext executorContext)
    {
        try
        {
            return residentHandlerClass.newInstance();
        }
        catch(InstantiationException | IllegalAccessException e)
        {
            // NOTE: this results in an error elsewhere
            logger.error("Failed to instantiate ResidentHandler: {}", residentHandlerClass.getSimpleName());
        }
        return null;
    }
}
