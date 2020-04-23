package com.comcast.pop.handler.reaper.impl.messages;

import com.comast.pop.handler.base.messages.MessageLookup;
import com.comast.pop.handler.base.messages.ResourceBundleStringRetriever;

public enum ReaperMessages implements MessageLookup
{
    NO_PODS_TO_REAP,
    POD_DELETE_ATTEMPT,
    POD_BATCH_REAP_FAILED;

    public static final String RESOURCE_PATH = "com/comcast/pop/handler/executor/reaperMessages";
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
