package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReader;

public class EnvironmentPayloadReader implements PayloadReader
{
    private LaunchDataWrapper launchDataWrapper;

    public EnvironmentPayloadReader(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    @Override
    public String readPayload()
    {
        return launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.PAYLOAD.name(), null);
    }
}
