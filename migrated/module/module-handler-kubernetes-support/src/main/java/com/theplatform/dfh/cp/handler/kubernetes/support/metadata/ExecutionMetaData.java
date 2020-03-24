package com.theplatform.dfh.cp.handler.kubernetes.support.metadata;

import com.theplatform.dfh.cp.handler.base.field.api.args.MetaData;
import com.theplatform.dfh.cp.handler.base.processor.HandlerMetadata;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ExecutionMetaData implements MetaData<Object>
{
    private static Logger logger = LoggerFactory.getLogger(ExecutionMetaData.class);
    private Map<String, Object> metaData = new HashMap<>();
    private final String handlerName;

    public ExecutionMetaData(String handlerName)
    {
        this.handlerName = handlerName;
    }

    public void extractRequestedCPUs(ExecutionConfig executionConfig)
    {
        String requestedCPUs = "0.";
        try
        {
            requestedCPUs =  executionConfig.getCpuRequestModulator().getCpuRequest();
        }
        catch (Exception e)
        {
            logger.error("Missing cpu request for "+handlerName +" utility pod",e);
        }
        metaData.put(HandlerMetadata.RequestedCPUs.name(),requestedCPUs);
    }

    @Override
    public Map<String, Object> getMetadata()
    {
        return metaData;
    }
}
