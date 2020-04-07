package com.comcast.pop.handler.kubernetes.support.payload;

import com.comast.pop.handler.base.payload.PayloadWriter;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;

import java.util.Map;

/**
 * Base for all PayloadWriters that operate with an ExecutionConfig
 */
public abstract class BaseExecutionConfigPayloadWriter implements PayloadWriter
{
    private ExecutionConfig executionConfig;

    public BaseExecutionConfigPayloadWriter(ExecutionConfig executionConfig)
    {
        this.executionConfig = executionConfig;
    }

    @Override
    public void writePayload(String payload)
    {
        executionConfig.getEnvVars().put(PayloadField.PAYLOAD_TYPE_ENV_VAR.getFieldName(), getPayloadType());
        writePayload(payload, executionConfig.getEnvVars());
    }

    protected abstract void writePayload(String payload, Map<String, String> destination);

    public ExecutionConfig getExecutionConfig()
    {
        return executionConfig;
    }
}
