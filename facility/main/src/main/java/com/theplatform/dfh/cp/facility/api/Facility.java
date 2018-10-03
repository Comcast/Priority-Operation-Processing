package com.theplatform.dfh.cp.facility.api;

import com.theplatform.dfh.cp.api.IdentifiedObject;

public class Facility implements IdentifiedObject
{
    private String id;
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
