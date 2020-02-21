package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReader;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReaderFactory;
import com.theplatform.dfh.cp.handler.kubernetes.support.payload.compression.CompressedEnvironmentPayloadReader;

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
        // if the PAYLOAD env var is there use it
        if(launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.PAYLOAD.name()) != null)
        {
            return new EnvironmentPayloadReader(launchDataWrapper);
        }
        return new CompressedEnvironmentPayloadReader(launchDataWrapper);
    }
}
