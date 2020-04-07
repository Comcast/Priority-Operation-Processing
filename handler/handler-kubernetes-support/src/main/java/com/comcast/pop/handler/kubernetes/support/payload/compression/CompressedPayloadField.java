package com.comcast.pop.handler.kubernetes.support.payload.compression;

import com.comast.pop.handler.base.field.retriever.api.NamedField;

public enum CompressedPayloadField implements NamedField
{
    PAYLOAD_COMPRESSION_MAX_SEGMENT_SIZE("payload.compression.maxSegmentSize");

    private final String fieldName;

    CompressedPayloadField(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
