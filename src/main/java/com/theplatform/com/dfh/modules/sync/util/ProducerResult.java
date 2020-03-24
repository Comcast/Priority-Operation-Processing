package com.theplatform.com.dfh.modules.sync.util;

import java.util.Collection;

public class ProducerResult<T>
{
    private boolean interrupted;
    private Collection<T> itemsProduced;

    public Collection<T> getItemsProduced()
    {
        return itemsProduced;
    }

    public ProducerResult<T> setItemsProduced(Collection<T> itemsProduced)
    {
        this.itemsProduced = itemsProduced;
        return this;
    }

    public boolean isInterrupted()
    {
        return interrupted;
    }

    public ProducerResult<T> setInterrupted(boolean interrupted)
    {
        this.interrupted = interrupted;
        return this;
    }
}
