package com.theplatform.dfh.cp.handler.executor.impl.processor.messages;

import com.theplatform.dfh.cp.handler.base.messages.ResourceBundleStringRetriever;
import com.theplatform.dfh.cp.handler.executor.impl.messages.ExecutorMessages;
import org.testng.annotations.Test;

public class ExecutorMessagesTest
{
    @Test
    public void verifyMessages()
    {
        new ResourceBundleStringRetriever(ExecutorMessages.RESOURCE_PATH).testAllEntries(ExecutorMessages.values());
    }
}
