package com.comcast.pop.modules.queue.api.access;

import com.comcast.pop.modules.queue.api.ItemQueue;
import com.comcast.pop.modules.queue.api.QueueResult;

/**
 * Provides view only queue access to an underlying ItemQueue
 */
public class ItemQueueViewer<T>
{
    private final ItemQueue<T> itemQueue;

    public ItemQueueViewer(ItemQueue<T> itemQueue)
    {
        this.itemQueue = itemQueue;
    }

    public QueueResult<T> peek()
    {
        return itemQueue.peek();
    }

    public QueueResult<T> size()
    {
        return itemQueue.size();
    }
}
