package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.payload.PayloadWriter;

import java.util.Map;

public class EnvironmentPayloadWriter implements PayloadWriter
{
    @Override
    public void writePayload(String payload, Map<String, String> outputMap)
    {
        outputMap.put(HandlerField.PAYLOAD.name(), payload);
    }
}
