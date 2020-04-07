package com.comast.pop.handler.base.payload;

/**
 * Writer for payload objects
 */
public interface PayloadWriter
{
    void writePayload(String payload);
    String getPayloadType();
}
