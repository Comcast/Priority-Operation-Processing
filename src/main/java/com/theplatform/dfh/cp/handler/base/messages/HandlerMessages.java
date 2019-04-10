package com.theplatform.dfh.cp.handler.base.messages;

public enum HandlerMessages implements MessageLookup
{
    GENERAL_HANDLER_ERROR;

    public static final String RESOURCE_PATH = "com/theplatform/handler/handlerMessages";
    private static final PropertyMessages propertyMessages = new PropertyMessages(RESOURCE_PATH);

    @Override
    public String getMessage(Object... args)
    {
        return propertyMessages.getMessage(this.name().toLowerCase(), args);
    }
}
