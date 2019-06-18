package com.theplatform.dfh.cp.handler.executor.impl.properties;

import com.theplatform.dfh.cp.handler.field.retriever.api.NamedField;

public enum ExecutorProperty implements NamedField
{
    EXECUTOR_REAP_SELF("executor.reap.self");

    private final String fieldName;

    ExecutorProperty(String fieldName)
    {
        this.fieldName = fieldName;
    }

    @Override
    public String getFieldName()
    {
        return fieldName;
    }
}
