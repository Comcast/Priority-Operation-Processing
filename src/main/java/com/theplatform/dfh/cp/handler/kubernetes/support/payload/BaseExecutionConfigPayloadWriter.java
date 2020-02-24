package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.payload.PayloadWriter;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;

import java.util.Map;

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
        writePayloadInternal(payload, executionConfig.getEnvVars());
    }

    protected abstract void writePayloadInternal(String payload, Map<String, String> destination);

    public ExecutionConfig getExecutionConfig()
    {
        return executionConfig;
    }
}
