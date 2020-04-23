package com.comast.pop.handler.base.messages;

import org.testng.annotations.Test;

public class HandlerMessagesTest
{
    @Test
    public void verifyMessages()
    {
        new ResourceBundleStringRetriever(HandlerMessages.RESOURCE_PATH).testAllEntries(HandlerMessages.values());
    }
}
