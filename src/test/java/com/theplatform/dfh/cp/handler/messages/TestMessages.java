package com.theplatform.dfh.cp.handler.messages;

import com.theplatform.dfh.cp.handler.base.messages.MessageLookup;
import com.theplatform.dfh.cp.handler.base.messages.PropertyMessages;

public enum TestMessages implements MessageLookup
{
    sample_message,
    sample_arg_message;

    public static final String RESOURCE_PATH = "com/theplatform/dfh/cp/handler/messages/test";

    private static PropertyMessages propertyMessages = new PropertyMessages(RESOURCE_PATH);

    @Override
    public String getMessage(Object... args)
    {
        return propertyMessages.getMessage(this.name(), args);
    }

    @Override
    public String toString()
    {
        return propertyMessages.getMessage(this.name());
    }
}
