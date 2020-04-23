package com.comcast.pop.handler.kubernetes.support.payload;

import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.payload.PayloadReader;
import com.comast.pop.handler.base.payload.PayloadReaderFactory;
import com.comcast.pop.handler.kubernetes.support.payload.compression.CompressedEnvironmentPayloadReader;

public class PayloadReaderFactoryImpl implements PayloadReaderFactory
{
    private LaunchDataWrapper launchDataWrapper;

    public PayloadReaderFactoryImpl(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    @Override
    public PayloadReader createReader()
    {
        String payloadTypeString = launchDataWrapper.getEnvironmentRetriever()
            .getField(PayloadField.PAYLOAD_TYPE_ENV_VAR.getFieldName(), PayloadType.ENV_VAR.getFieldName());

        PayloadType payloadType = PayloadType.determinePayloadType(payloadTypeString, PayloadType.ENV_VAR);

        switch(payloadType)
        {
            case COMPRESSED_ENV_VAR:
                return new CompressedEnvironmentPayloadReader(launchDataWrapper);
            case ENV_VAR:
            default:
                return new EnvironmentPayloadReader(launchDataWrapper);
        }
    }
}
