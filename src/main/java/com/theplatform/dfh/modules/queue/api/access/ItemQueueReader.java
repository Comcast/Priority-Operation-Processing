package com.theplatform.dfh.modules.queue.api.access;

import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.QueueResult;

/**
 * Provides read-from-queue access to an underlying ItemQueue
 */
public class ItemQueueReader<T>
{
    private final ItemQueue<T> itemQueue;
    private int pollCount = 0;
    private int successiveEmptyPollCount = 0;

    public ItemQueueReader(ItemQueue<T> itemQueue)
    {
        this.itemQueue = itemQueue;
    }

    public QueueResult poll()
    {
        pollCount++;
        QueueResult queueResult = itemQueue.poll();
        if(queueResult.isSuccessful()
            && queueResult.getData() != null
            && queueResult.getData().size() > 0)
        {
            successiveEmptyPollCount = 0;
        }
        else
        {
            successiveEmptyPollCount++;
        }
        return queueResult;
    }

    public int getPollCount()
    {
        return pollCount;
    }

    public int getSuccessiveEmptyPollCount()
    {
        return successiveEmptyPollCount;
    }
}
