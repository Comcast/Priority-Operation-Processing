package com.comcast.pop.modules.queue.api;

public interface ItemQueueFactory<T>
{
    ItemQueue<T> createItemQueue(String name);
}
