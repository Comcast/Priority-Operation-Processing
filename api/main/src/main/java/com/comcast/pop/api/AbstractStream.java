package com.comcast.pop.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.comcast.pop.api.params.ParamsMap;

public abstract class AbstractStream
{
    private String reference;
    private String type;
    private ParamsMap params = new ParamsMap();

    @JsonIgnore
    public String getReference()
    {
        return reference;
    }

    @JsonIgnore
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

    public void addParam(String key, Object value)
    {
        if (params == null)
            params = new ParamsMap();

        params.put(key, value);
    }
}
