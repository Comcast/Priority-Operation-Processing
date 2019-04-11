package com.theplatform.dfh.cp.handler.executor.impl.messages;

import com.theplatform.dfh.cp.handler.base.messages.MessageLookup;
import com.theplatform.dfh.cp.handler.base.messages.ResourceBundleStringRetriever;

public enum ExecutorMessages implements MessageLookup
{
    AGENDA_LOADING,
    AGENDA_LOADED,
    AGENDA_LOAD_FAIL,
    AGENDA_LOAD_INVALID,
    AGENDA_NO_OPERATIONS,
    OPERATIONS_RUNNING,
    OPERATIONS_ERROR,
    OPERATION_EXECUTION_ERROR;

    public static final String RESOURCE_PATH = "com/theplatform/handler/executor/executorMessages";
    private static final ResourceBundleStringRetriever stringRetriever = new ResourceBundleStringRetriever(RESOURCE_PATH);

    private final String key = name().toLowerCase();

    @Override
    public String getMessage(Object... args)
    {
        return stringRetriever.getMessage(getKey(), args);
    }

    @Override
    public String getKey()
    {
        return key;
    }
}
