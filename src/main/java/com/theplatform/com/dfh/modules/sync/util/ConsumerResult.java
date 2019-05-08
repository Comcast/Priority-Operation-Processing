package com.theplatform.com.dfh.modules.sync.util;

public class ConsumerResult<T>
{
    private int itemsConsumedCount;
    private boolean interrupt;

    public int getItemsConsumedCount()
    {
        return itemsConsumedCount;
    }

    public ConsumerResult<T> setItemsConsumedCount(int itemsConsumedCount)
    {
        this.itemsConsumedCount = itemsConsumedCount;
        return this;
    }

    public boolean isInterrupt()
    {
        return interrupt;
    }

    public ConsumerResult<T> setInterrupt(boolean interrupt)
    {
        this.interrupt = interrupt;
        return this;
    }
}
