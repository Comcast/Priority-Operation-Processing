package com.theplatform.dfh.cp.handler.kubernetes.support.payload;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.NamedField;
import org.apache.commons.lang3.StringUtils;

public enum PayloadType implements NamedField
{
    ENV_VAR("envVar"),
    COMPRESSED_ENV_VAR("compressedEnvVar");

    private final String fieldName;

    PayloadType(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public static PayloadType determinePayloadType(String input, PayloadType defaultType)
    {
        for(PayloadType payloadType : PayloadType.values())
        {
            if(StringUtils.equalsIgnoreCase(input, payloadType.fieldName))
                return payloadType;
        }
        return defaultType;
    }
}
