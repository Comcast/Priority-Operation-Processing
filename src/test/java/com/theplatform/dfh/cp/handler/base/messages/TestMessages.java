package com.theplatform.dfh.cp.handler.base.messages;

public enum TestMessages implements MessageLookup
{
    SAMPLE_MESSAGE,
    SAMPLE_ARG_MESSAGE;

    public static final String RESOURCE_PATH = "com/theplatform/dfh/cp/handler/base/messages/test";
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
