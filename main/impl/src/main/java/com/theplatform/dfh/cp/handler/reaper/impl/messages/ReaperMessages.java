package com.theplatform.dfh.cp.handler.reaper.impl.messages;

import com.theplatform.dfh.cp.handler.base.messages.MessageLookup;
import com.theplatform.dfh.cp.handler.base.messages.ResourceBundleStringRetriever;

public enum ReaperMessages implements MessageLookup
{
    AGENDA_NO_OPERATIONS,
    OPERATIONS_RUNNING,
    OPERATIONS_ERROR,
    OPERATION_EXECUTION_ERROR,
    OPERATION_RESIDENT_EXECUTION_FAILED;

    public static final String RESOURCE_PATH = "com/theplatform/handler/executor/reaperMessages";
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
