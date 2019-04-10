package com.theplatform.dfh.cp.handler.base.messages;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Wrapper for loading and using a ResourceBundle for looking up strings.
 */
public class PropertyMessages
{
    private final ResourceBundle bundle;

    public PropertyMessages(String bundleName)
    {
        bundle = ResourceBundle.getBundle(bundleName);
    }

    /**
     * Gets the message from the resource bundle
     * @param key The key to get from the resource bundle
     * @param args (optional) args for a MessageFormat style string
     * @return The resulting string
     */
    public String getMessage(String key, Object... args)
    {
        String message = bundle.getString(key);
        if (args != null && args.length > 0)
            return MessageFormat.format(message, args);
        else
            return message;
    }

    /**
     * Gets all of the entries based on the name of the supplied enum(s)
     * This is a utility method to help with testing that a property file is valid
     * @param enumEntries The enums to get
     */
    public void getAllEntries(Enum[] enumEntries)
    {
        for(Enum e : enumEntries) getMessage(e.name());
    }
}
