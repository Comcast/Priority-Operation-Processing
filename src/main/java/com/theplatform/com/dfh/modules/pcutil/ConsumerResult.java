package com.theplatform.com.dfh.modules.pcutil;

public class ConsumerResult<T>
{
    private int itemsConsumedCount;
    private boolean interrupt;

    public int getItemsConsumedCount()
    {
        return itemsConsumedCount;
    }

    public ConsumerResult setItemsConsumedCount(int itemsConsumedCount)
    {
        this.itemsConsumedCount = itemsConsumedCount;
        return this;
    }

    public boolean isInterrupt()
    {
        return interrupt;
    }

    public ConsumerResult setInterrupt(boolean interrupt)
    {
        this.interrupt = interrupt;
        return this;
    }
}
