package com.theplatform.dfh.cp.handler.executor.impl.messages;

import com.theplatform.dfh.cp.handler.base.messages.MessageLookup;
import com.theplatform.dfh.cp.handler.base.messages.ResourceBundleStringRetriever;

public enum ExecutorMessages implements MessageLookup
{
    AGENDA_NO_OPERATIONS,
    OPERATIONS_RUNNING,
    OPERATIONS_ERROR,
    OPERATION_EXECUTION_ERROR,
    OPERATION_EXECUTION_INCOMPLETE,
    OPERATION_EXECUTION_INCOMPLETE_NO_PROGRESS,
    OPERATION_RESIDENT_EXECUTION_FAILED,
    KUBERNETES_FOLLOW_ERROR,
    KUBERNETES_POD_FAILED;

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
