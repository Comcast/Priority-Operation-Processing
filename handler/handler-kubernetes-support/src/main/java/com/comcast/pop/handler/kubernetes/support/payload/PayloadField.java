package com.comcast.pop.handler.kubernetes.support.payload;

import com.comast.pop.handler.base.field.retriever.api.NamedField;

public enum PayloadField implements NamedField
{
    // specifies the type of output this component should use (generally a property)
    PAYLOAD_OUTPUT_TYPE_PROPERTY("pop.payload.outputType"),
    // specifies the type of payload this component should attempt to read (generally an env var)
    PAYLOAD_TYPE_ENV_VAR("POP_PAYLOAD_TYPE");

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
