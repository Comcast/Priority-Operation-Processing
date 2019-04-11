package com.theplatform.dfh.cp.handler.messages;

import com.theplatform.dfh.cp.handler.base.messages.HandlerMessages;
import com.theplatform.dfh.cp.handler.base.messages.ResourceBundleStringRetriever;
import org.testng.annotations.Test;

public class HandlerMessagesTest
{
    @Test
    public void verifyMessages()
    {
        new ResourceBundleStringRetriever(HandlerMessages.RESOURCE_PATH).testAllEntries(HandlerMessages.values());
    }
}
