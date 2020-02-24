package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;

import java.util.Map;

public class EnvironmentPayloadWriter extends BaseExecutionConfigPayloadWriter
{
    public EnvironmentPayloadWriter(ExecutionConfig executionConfig)
    {
        super(executionConfig);
    }

    @Override
    public void writePayloadInternal(String payload, Map<String, String> outputMap)
    {
        outputMap.put(HandlerField.PAYLOAD.name(), payload);
    }

    @Override
    public String getPayloadType()
    {
        return PayloadType.ENV_VAR.getFieldName();
    }
}
