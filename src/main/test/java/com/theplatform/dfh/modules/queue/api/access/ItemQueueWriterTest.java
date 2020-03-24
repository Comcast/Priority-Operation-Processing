package com.theplatform.dfh.modules.queue.api.access;

import com.theplatform.dfh.modules.queue.api.ItemQueue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ItemQueueWriterTest
{
    private ItemQueueWriter<String> itemQueueWriter;
    private ItemQueue<String> mockItemQueue;

    @BeforeMethod
    public void setup()
    {
        mockItemQueue = mock(ItemQueue.class);
        itemQueueWriter = new ItemQueueWriter<>(mockItemQueue);
    }

    @Test
    public void verifyAddCall()
    {
        final String ITEM = "item";

        itemQueueWriter.add(ITEM);
        verify(mockItemQueue, times(1)).add(ITEM);
    }
}
