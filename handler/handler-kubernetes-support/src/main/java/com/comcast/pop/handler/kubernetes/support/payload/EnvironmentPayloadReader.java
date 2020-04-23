package com.comcast.pop.handler.kubernetes.support.payload;

import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.payload.PayloadReader;

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
