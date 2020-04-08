package com.comcast.pop.handler.kubernetes.support.payload;

import com.comast.pop.handler.base.field.api.HandlerField;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;

import java.util.Map;

/**
 * Basic payload writer that sets the payload on a single environment variable
 */
public class EnvironmentPayloadWriter extends BaseExecutionConfigPayloadWriter
{
    public EnvironmentPayloadWriter(ExecutionConfig executionConfig)
    {
        super(executionConfig);
    }

    @Override
    public void writePayload(String payload, Map<String, String> outputMap)
    {
        outputMap.put(HandlerField.PAYLOAD.name(), payload);
    }

    @Override
    public String getPayloadType()
    {
        return PayloadType.ENV_VAR.getFieldName();
    }
}
