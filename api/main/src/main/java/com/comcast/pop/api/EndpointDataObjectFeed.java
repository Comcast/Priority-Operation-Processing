package com.comcast.pop.api;

import java.util.Collection;

public class EndpointDataObjectFeed<D extends DefaultEndpointDataObject>
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
