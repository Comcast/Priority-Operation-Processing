package com.comcast.pop.cp.endpoint.util;

import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.RuntimeServiceException;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ServiceResponse;

/**
 * ServiceResponse factory to wrap the creatton of ServiceResponses. No per-type implementation is necessary unless special handling is required.
 * @param <R> The type of the ServiceResponse
 */
public class ServiceResponseFactory<R extends ServiceResponse<ErrorResponse>>
{
    private Class<R> responseClass;

    public ServiceResponseFactory(Class<R> responseClass)
    {
        this.responseClass = responseClass;
    }

    public R createResponse(ServiceRequest serviceRequest, ErrorResponse errorResponse, String errorResponsePrefix)
    {
        if(errorResponsePrefix != null && errorResponse != null && errorResponse.getDescription() != null)
        {
            errorResponse.setDescription(errorResponsePrefix + " " + errorResponse.getDescription());
        }
        try
        {
            R response = responseClass.newInstance();
            response.setCID(serviceRequest.getCID());
            response.setErrorResponse(errorResponse);
            return response;
        }
        catch(Exception e)
        {
            throw new RuntimeServiceException(String.format("Failed to create response of type %1$s", responseClass.getSimpleName()), e, 500);
        }
    }
}
