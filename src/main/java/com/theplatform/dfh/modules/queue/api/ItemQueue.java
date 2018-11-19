package com.theplatform.dfh.modules.queue.api;

import java.util.Collection;

/**
 * Limited interface for a queue (based on the java Queue interface)
 */
public interface ItemQueue<T>
{
    /**
     * Adds the specified item to the Queue
     * @param item The item to add
     * @return true on success, false otherwise
     */
    QueueResult<T> add(T item);

    /**
     * Adds the specified item to the Queue
     * @param items The items to add
     * @return true on success, false otherwise
     */
    QueueResult<T> add(Collection<T> items);

    /**
     * Returns the next item of the queue but does not remove it
     * @return head of the queue or none if the queue is empty
     */
    QueueResult<T> peek();

    /**
     * Gets the next item of the queue and removes it
     * @return head of the queue or none if the queue is empty
     */
    QueueResult<T> poll();

    /**
     * Gets the next number of items, up to the specified max
     * @param maxPollCount The maximum number of items to poll the queue for
     * @return head maxPollCount (or fewer) items or none if queue is empty
     */
    QueueResult<T> poll(int maxPollCount);

    /**
     * Gets the number of items in the queue
     * @return The number of items in the queue
     */
    QueueResult<T> size();
}
