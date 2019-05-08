package com.theplatform.com.dfh.modules.sync.util;

public class ConsumerResult<T>
{
    private int itemsConsumedCount;
    private boolean interrupted;

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
}
