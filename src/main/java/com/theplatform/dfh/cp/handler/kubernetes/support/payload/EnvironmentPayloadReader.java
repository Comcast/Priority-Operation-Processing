package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReader;

/**
 * Basic payload writer that reads the payload from a single environment variable
 */
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
