package com.theplatform.dfh.cp.handler.executor.impl.messages;

import com.theplatform.dfh.cp.handler.base.messages.ResourceBundleStringRetriever;
import com.theplatform.dfh.cp.handler.reaper.impl.messages.ReaperMessages;
import org.testng.annotations.Test;

public class ReaperMessagesTest
{
    @Test
    public void verifyMessages()
    {
        new ResourceBundleStringRetriever(ReaperMessages.RESOURCE_PATH).testAllEntries(ReaperMessages.values());
    }
}
