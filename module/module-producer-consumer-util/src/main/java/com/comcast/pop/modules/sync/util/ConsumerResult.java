package com.comcast.pop.modules.sync.util;

import java.util.Collection;

public class ConsumerResult<T>
{
    private int itemsConsumedCount;
    private boolean interrupted;
    private Collection<T> unprocessedItems;

    public int getItemsConsumedCount()
    {
        return itemsConsumedCount;
    }

    public ConsumerResult<T> setItemsConsumedCount(int itemsConsumedCount)
    {
        this.itemsConsumedCount = itemsConsumedCount;
        return this;
    }

    public boolean isInterrupted()
    {
        return interrupted;
    }

    public ConsumerResult<T> setInterrupted(boolean interrupted)
    {
        this.interrupted = interrupted;
        return this;
    }

    public Collection<T> getUnprocessedItems()
    {
        return unprocessedItems;
    }

    public ConsumerResult<T> setUnprocessedItems(Collection<T> unprocessedItems)
    {
        this.unprocessedItems = unprocessedItems;
        return this;
    }
}
