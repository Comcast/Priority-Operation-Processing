package com.comcast.pop.endpoint.api;

import com.comcast.pop.api.params.ParamsMap;

/**
 * Standard response object for persistence
 */
public class ObjectPersistResponse
{
    private String id;
    private ParamsMap params;

    public ObjectPersistResponse()
    {
    }

    public ObjectPersistResponse(String id)
    {
        this.id = id;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public ParamsMap getParams()
    {
        return params;
    }

    public void setParams(ParamsMap params)
    {
        this.params = params;
    }
}
