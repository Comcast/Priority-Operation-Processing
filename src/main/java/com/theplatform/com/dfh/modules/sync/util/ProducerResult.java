package com.theplatform.com.dfh.modules.sync.util;

import java.util.Collection;

public class ProducerResult<T>
{
    private boolean interrupt;
    private Collection<T> itemsProduced;

    public Collection<T> getItemsProduced()
    {
        return itemsProduced;
    }

    public ProducerResult setItemsProduced(Collection<T> itemsProduced)
    {
        this.itemsProduced = itemsProduced;
        return this;
    }

    public boolean isInterrupt()
    {
        return interrupt;
    }

    public ProducerResult setInterrupt(boolean interrupt)
    {
        this.interrupt = interrupt;
        return this;
    }
}
