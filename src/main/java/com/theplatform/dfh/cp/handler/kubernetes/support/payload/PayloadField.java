package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.NamedField;

public enum PayloadField implements NamedField
{
    PAYLOAD_COMPRESSION_MAX_SEGMENT_SIZE("payload.compression.maxSegmentSize"),
    PAYLOAD_COMPRESSION_ENABLED("payload.compression.enabled");

    private final String fieldName;

    PayloadField(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
