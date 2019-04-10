package com.theplatform.dfh.cp.handler.messages;

import com.theplatform.dfh.cp.handler.base.messages.MessageLookup;
import com.theplatform.dfh.cp.handler.base.messages.PropertyMessages;

public enum TestMessages implements MessageLookup
{
    SAMPLE_MESSAGE,
    SAMPLE_ARG_MESSAGE;

    public static final String RESOURCE_PATH = "com/theplatform/dfh/cp/handler/messages/test";
    private static final PropertyMessages propertyMessages = new PropertyMessages(RESOURCE_PATH);

    @Override
    public String getMessage(Object... args)
    {
        return propertyMessages.getMessage(this.name().toLowerCase(), args);
    }
}
