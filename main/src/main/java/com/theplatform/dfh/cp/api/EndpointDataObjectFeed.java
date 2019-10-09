package com.theplatform.dfh.cp.api;

import java.util.Collection;

public class EndpointDataObjectFeed<D extends EndpointDataObject>
{
    private Collection<D> entries;
    public EndpointDataObjectFeed(){}

    public EndpointDataObjectFeed(Collection<D> entries)
    {
        this.entries = entries;
    }

    public Collection<D> getEntries()
    {
        return entries;
    }

    public void setEntries(Collection<D> entries)
    {
        this.entries = entries;
    }
}
