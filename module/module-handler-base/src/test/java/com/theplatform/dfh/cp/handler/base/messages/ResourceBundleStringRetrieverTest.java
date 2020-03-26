package com.theplatform.dfh.cp.handler.base.messages;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.MissingResourceException;

public class ResourceBundleStringRetrieverTest
{
    private ResourceBundleStringRetriever propertyMessages = new ResourceBundleStringRetriever(TestMessages.RESOURCE_PATH);

    @Test
    public void testGetMessageEnum()
    {
        Assert.assertEquals(TestMessages.SAMPLE_MESSAGE.getMessage(), "hello world!");
    }

    @Test
    public void testGetMessageArgEnum()
    {
        final String arg = "thePlatform!";
        Assert.assertEquals(TestMessages.SAMPLE_ARG_MESSAGE.getMessage(arg), "hello " + arg);
    }

    @Test(expectedExceptions = MissingResourceException.class, expectedExceptionsMessageRegExp = ".*key unknown.*")
    public void testMissingMessage()
    {
        propertyMessages.getMessage("unknown");
    }

    @Test
    public void testAllMessages()
    {
        Arrays.stream(TestMessages.values()).forEach(TestMessages::getMessage);
    }
}
