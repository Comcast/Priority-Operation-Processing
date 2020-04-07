package com.comcast.pop.handler.kubernetes.support.payload;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.payload.PayloadWriter;
import com.comast.pop.handler.base.payload.PayloadWriterFactory;
import com.comcast.pop.handler.kubernetes.support.payload.compression.CompressedEnvironmentPayloadWriter;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;

public class PayloadWriterFactoryImpl implements PayloadWriterFactory<ExecutionConfig>
{
    private LaunchDataWrapper launchDataWrapper;

    public PayloadWriterFactoryImpl(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    @Override
    public PayloadWriter createWriter(ExecutionConfig executionConfig)
    {
        // get the output type, defaulting to writing to the env var
        String payloadTypeString = launchDataWrapper.getPropertyRetriever().getField(PayloadField.PAYLOAD_OUTPUT_TYPE_PROPERTY, PayloadType.ENV_VAR.getFieldName());

        PayloadType payloadType = PayloadType.determinePayloadType(payloadTypeString, PayloadType.ENV_VAR);

        switch(payloadType)
        {
            case COMPRESSED_ENV_VAR:
                return new CompressedEnvironmentPayloadWriter(executionConfig, launchDataWrapper);
            case ENV_VAR:
            default:
                return new EnvironmentPayloadWriter(executionConfig);
        }
    }
}
