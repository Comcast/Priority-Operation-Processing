package com.comast.pop.handler.base.messages;

public enum HandlerMessages implements MessageLookup
{
    GENERAL_HANDLER_ERROR,
    PAYLOAD_TRANSLATION_FAIL;

    public static final String RESOURCE_PATH = "com/comcast/pop/handler/handlerMessages";
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
