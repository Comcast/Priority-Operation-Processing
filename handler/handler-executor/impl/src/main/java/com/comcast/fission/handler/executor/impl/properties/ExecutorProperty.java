package com.comcast.fission.handler.executor.impl.properties;

import com.theplatform.dfh.cp.handler.base.field.retriever.api.NamedField;

public enum ExecutorProperty implements NamedField
{
    REAP_SELF("executor.reap.self"),
    PROGRESS_THREAD_EXIT_TIMEOUT_MS("executor.progress.threadExitTimeoutMs");

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
