package com.theplatform.dfh.cp.handler.base.payload;

import java.util.Map;

public interface PayloadWriter
{
    // TODO: this might be too specific to kubernetes (the outputmap param)
    void writePayload(String payload, Map<String, String> outputMap);
}
