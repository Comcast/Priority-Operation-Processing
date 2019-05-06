package com.theplatform.dfh.cp.handler.reaper.impl.property;

import com.theplatform.dfh.cp.handler.field.retriever.api.NamedField;

public enum ReaperProperty implements NamedField
{
    REAPER_RUN_MAX_MINUTES("reaper.run.maxminutes"),
    POD_REAP_AGE_MINUTES("pod.reap.age.minutes"),
    POD_REAP_BATCH_SIZE("pod.reap.batch.size");

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
