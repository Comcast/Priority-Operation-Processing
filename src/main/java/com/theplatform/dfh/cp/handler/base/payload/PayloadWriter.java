package com.theplatform.dfh.cp.handler.base.payload;

/**
 * Writer for payload objects
 */
public interface PayloadWriter
{
    void writePayload(String payload);
    String getPayloadType();
}
