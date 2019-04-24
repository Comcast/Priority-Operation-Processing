package com.theplatform.dfh.cp.handler.reaper.impl.property;

import com.theplatform.dfh.cp.handler.field.retriever.api.NamedField;

public enum ReaperProperty implements NamedField
{
    POD_REAP_AGE_MINUTES("pod.reap.age.minutes");

    private final String fieldName;

    ReaperProperty(String fieldName)
    {
        this.fieldName = fieldName;
    }

    public String getFieldName()
    {
        return fieldName;
    }
}
