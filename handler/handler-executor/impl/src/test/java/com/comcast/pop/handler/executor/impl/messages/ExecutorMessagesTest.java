package com.comcast.pop.handler.executor.impl.messages;

import com.comast.pop.handler.base.messages.ResourceBundleStringRetriever;
import org.testng.annotations.Test;

public class ExecutorMessagesTest
{
    @Test
    public void verifyMessages()
    {
        new ResourceBundleStringRetriever(ExecutorMessages.RESOURCE_PATH).testAllEntries(ExecutorMessages.values());
    }
}
