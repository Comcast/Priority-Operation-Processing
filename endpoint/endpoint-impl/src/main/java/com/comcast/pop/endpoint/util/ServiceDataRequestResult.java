package com.comcast.pop.endpoint.util;

import com.comcast.pop.endpoint.api.ServiceResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.object.api.IdentifiedObject;

/**
 * Wrapper object for request processors that either need a DataObjectResponse(success) or a ServiceResponse(error)
 * @param <D> The type of object the DataObjectResponse contains
 * @param <R> The type of ServiceResponse
 */
public class ServiceDataRequestResult<D extends IdentifiedObject, R extends ServiceResponse>
{
    private DataObjectResponse<D> dataObjectResponse;
    private R serviceResponse;

    public DataObjectResponse<D> getDataObjectResponse()
    {
        return dataObjectResponse;
    }

    public void setDataObjectResponse(DataObjectResponse<D> dataObjectResponse)
    {
        this.dataObjectResponse = dataObjectResponse;
    }

    public R getServiceResponse()
    {
        return serviceResponse;
    }

    public void setServiceResponse(R serviceResponse)
    {
        this.serviceResponse = serviceResponse;
    }
}
