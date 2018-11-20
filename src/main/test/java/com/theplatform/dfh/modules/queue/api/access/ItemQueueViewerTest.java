package com.theplatform.dfh.modules.queue.api.access;

import com.theplatform.dfh.modules.queue.api.ItemQueue;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ItemQueueViewerTest
{
    private ItemQueueViewer<String> itemQueueViewer;
    private ItemQueue<String> mockItemQueue;

    @BeforeMethod
    public void setup()
    {
        mockItemQueue = mock(ItemQueue.class);
        itemQueueViewer = new ItemQueueViewer<>(mockItemQueue);
    }

    @Test
    public void verifyPeekCall()
    {
        itemQueueViewer.peek();
        verify(mockItemQueue, times(1)).peek();
    }

    @Test
    public void verifySizeCall()
    {
        itemQueueViewer.size();
        verify(mockItemQueue, times(1)).size();
    }
}
