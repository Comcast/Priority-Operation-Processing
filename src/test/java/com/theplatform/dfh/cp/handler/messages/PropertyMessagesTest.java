package com.theplatform.dfh.cp.handler.messages;

import com.theplatform.dfh.cp.handler.base.messages.PropertyMessages;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.stream.Collectors;

public class PropertyMessagesTest
{
    private PropertyMessages propertyMessages = new PropertyMessages(TestMessages.RESOURCE_PATH);

    @Test
    public void testGetMessageEnum()
    {
        Assert.assertEquals(TestMessages.sample_message.toString(), "hello world!");
    }

    @Test
    public void testGetMessageArgEnum()
    {
        final String arg = "thePlatform!";
        Assert.assertEquals(TestMessages.sample_arg_message.getMessage(arg), "hello " + arg);
    }

    @Test(expectedExceptions = MissingResourceException.class, expectedExceptionsMessageRegExp = ".*key unknown.*")
    public void testMissingMessage()
    {
        propertyMessages.getMessage("unknown");
    }

    @Test
    public void testAllMessages()
    {
        propertyMessages.verifyAllMessagesExist(
            Arrays.stream(TestMessages.values())
                .map(TestMessages::name)
                .collect(Collectors.toList()));
    }
}
