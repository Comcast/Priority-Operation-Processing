package com.theplatform.dfh.cp.handler.base.messages;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

public class PropertyMessages
{
    private final ResourceBundle bundle;

    public PropertyMessages(String bundleName)
    {
        bundle = ResourceBundle.getBundle(bundleName);
    }

    public String getMessage(String key, Object... args)
    {
        String message = bundle.getString(key);
        if (args != null && args.length > 0)
            return MessageFormat.format(message, args);
        else
            return message;
    }

    public void verifyAllMessagesExist(List<String> messageNames)
    {
        for(String message : messageNames) getMessage(message);
    }
}
