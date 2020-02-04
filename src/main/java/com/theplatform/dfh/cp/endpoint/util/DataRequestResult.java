package com.theplatform.dfh.cp.endpoint.util;

import com.theplatform.dfh.endpoint.api.ServiceResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;

/**
 * Wrapper object for request processors that either need a DataObjectResponse(success) or a ServiceResponse(error)
 * @param <D> The type of object the DataObjectResponse contains
 * @param <SR> The type of ServiceResponse
 */
public class DataRequestResult<D extends IdentifiedObject, SR extends ServiceResponse>
{
    private DataObjectResponse<D> dataObjectResponse;
    private SR serviceResponse;

    public DataObjectResponse<D> getDataObjectResponse()
    {
        return dataObjectResponse;
    }

    public void setDataObjectResponse(DataObjectResponse<D> dataObjectResponse)
    {
        this.dataObjectResponse = dataObjectResponse;
    }

    public SR getServiceResponse()
    {
        return serviceResponse;
    }

    public void setServiceResponse(SR serviceResponse)
    {
        this.serviceResponse = serviceResponse;
    }
}
