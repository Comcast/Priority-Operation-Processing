package com.comcast.fission.endpoint.api;

import com.theplatform.dfh.cp.api.EndpointDataObject;

import java.util.Collection;

public class DataObjectFeedServiceResponse <D extends EndpointDataObject> extends DefaultServiceResponse
{
    private Collection<D> dataObjects;

    public DataObjectFeedServiceResponse(){}

    public DataObjectFeedServiceResponse(ErrorResponse errorResponse)
    {
        setErrorResponse(errorResponse);
    }

    public DataObjectFeedServiceResponse(Collection<D> dataObjects)
    {
        this.dataObjects = dataObjects;
    }

    public Collection<D> getAll()
    {
        return dataObjects;
    }

    public void setAll(Collection<D> dataObjects)
    {
        this.dataObjects = dataObjects;
    }
}
