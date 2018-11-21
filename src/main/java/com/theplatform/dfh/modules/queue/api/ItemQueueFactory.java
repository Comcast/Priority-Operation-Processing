package com.theplatform.dfh.modules.queue.api;

public interface ItemQueueFactory<T>
{
    ItemQueue<T> createItemQueue(String name);
}
