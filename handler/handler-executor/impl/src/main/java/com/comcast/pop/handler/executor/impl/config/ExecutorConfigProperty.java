package com.comcast.pop.handler.executor.impl.config;

import com.comast.pop.handler.base.field.retriever.api.NamedField;

public enum ExecutorConfigProperty implements NamedField
{
    EXPAND_AGENDA_ATTEMPTS("resourcePool.expandAgenda.attemptCount"),
    EXPAND_AGENDA_DELAY_MS("resourcePool.expandAgenda.delayMs");

    private String fieldName;

    ExecutorConfigProperty(String fieldName)
    {
        this.fieldName = fieldName;
    }

    @Override
    public String getFieldName()
    {
        return fieldName;
    }
}
