package com.theplatform.dfh.cp.api;

import java.util.Collection;

public class EndpointDataObjectFeed<D extends EndpointDataObject>
{
    private Collection<EndpointDataObject> entries;
    public EndpointDataObjectFeed(){}

    public EndpointDataObjectFeed(Collection<EndpointDataObject> entries)
    {
        this.entries = entries;
    }

    public Collection<EndpointDataObject> getEntries()
    {
        return entries;
    }

    public void setEntries(Collection<EndpointDataObject> entries)
    {
        this.entries = entries;
    }
}
