package com.theplatform.dfh.cp.api;

import com.theplatform.dfh.cp.api.params.ParamsMap;

public abstract class AbstractStream
{
    private String reference;
    private String type;
    private ParamsMap params = new ParamsMap();


    public String getReference()
    {
        return reference;
    }

    public void setReference(String reference)
    {
        this.reference = reference;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public ParamsMap getParams()
    {
        return params;
    }
}
