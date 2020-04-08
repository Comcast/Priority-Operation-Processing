package com.comcast.pop.modules.queue.api.access;

import com.comcast.pop.modules.queue.api.ItemQueue;
import com.comcast.pop.modules.queue.api.QueueResult;

import java.util.Collection;

/**
 * Provides write access to an underlying ItemQueue
 */
public class ItemQueueWriter<T>
{
    private final ItemQueue<T> itemQueue;

    public ItemQueueWriter(ItemQueue<T> itemQueue)
    {
        this.itemQueue = itemQueue;
    }

    public QueueResult<T> add(T item)
    {
        return itemQueue.add(item);
    }

    public QueueResult<T> add(Collection<T> items)
    {
        return itemQueue.add(items);
    }
}
