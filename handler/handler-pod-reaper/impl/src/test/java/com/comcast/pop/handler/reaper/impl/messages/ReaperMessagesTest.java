package com.comcast.pop.handler.reaper.impl.messages;

import com.comast.pop.handler.base.messages.ResourceBundleStringRetriever;
import org.testng.annotations.Test;

public class ReaperMessagesTest
{
    @Test
    public void verifyMessages()
    {
        new ResourceBundleStringRetriever(ReaperMessages.RESOURCE_PATH).testAllEntries(ReaperMessages.values());
    }
}
