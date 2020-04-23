package com.comcast.pop.modules.queue.api.access;

import com.comcast.pop.modules.queue.api.ItemQueue;
import com.comcast.pop.modules.queue.api.QueueResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemQueueReaderTest
{
    private ItemQueueReader<String> itemQueueReader;
    private ItemQueue<String> mockItemQueue;

    @BeforeMethod
    public void setup()
    {
        mockItemQueue = mock(ItemQueue.class);
        itemQueueReader = new ItemQueueReader<>(mockItemQueue);
    }

    @Test
    public void verifyEmptyPollCall()
    {
        Assert.assertEquals(itemQueueReader.getPollCount(), 0);
        itemQueueReader.poll();
        verify(mockItemQueue, times(1)).poll();
        Assert.assertEquals(itemQueueReader.getPollCount(), 1);
        Assert.assertEquals(itemQueueReader.getSuccessiveEmptyPollCount(), 1);
    }

    @Test
    public void verifyPollCall()
    {
        Assert.assertEquals(itemQueueReader.getPollCount(), 0);
        itemQueueReader.poll();
        Assert.assertEquals(itemQueueReader.getPollCount(), 1);
        Assert.assertEquals(itemQueueReader.getSuccessiveEmptyPollCount(), 1);

        QueueResult<String> mockResult = new QueueResult<String>()
            .setSuccessful(true)
            .setData(Collections.singletonList("a string"));

        when(mockItemQueue.poll()).thenReturn(mockResult);
        itemQueueReader.poll();
        Assert.assertEquals(itemQueueReader.getPollCount(), 2);
        Assert.assertEquals(itemQueueReader.getSuccessiveEmptyPollCount(), 0);
    }
}
