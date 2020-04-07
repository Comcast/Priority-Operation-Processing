package com.comast.pop.handler.base.messages;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Wrapper for loading and using a ResourceBundle for looking up strings.
 */
public class ResourceBundleStringRetriever
{
    private final ResourceBundle bundle;

    public ResourceBundleStringRetriever(String bundleName)
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
     * @param messageLookups The enums to get
     */
    public void testAllEntries(MessageLookup[] messageLookups)
    {
        List<String> failedEntries = new ArrayList<>();
        for(MessageLookup messageLookup : messageLookups)
        {
            try
            {
                getMessage(messageLookup.getKey());
            }
            catch(Exception e)
            {
                failedEntries.add(messageLookup.getKey());
            }
        }
        if(failedEntries.size() > 0)
        {
            throw new RuntimeException("Failed Entries: " + String.join(",", failedEntries));
        }
    }
}
