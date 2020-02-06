package com.theplatform.dfh.cp.endpoint.util;

import com.theplatform.dfh.cp.api.DefaultEndpointDataObject;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ServiceResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;

/**
 * Generic object retriever for use within a request processor to access data from other request processors.
 * This is primarily useful as most of the responses to a data request are consistent.
 * @param <R> The type of the ServiceResponse object
 */
public class ServiceDataObjectRetriever<R extends ServiceResponse<ErrorResponse>>
{
    private ServiceResponseFactory<R> serviceResponseFactory;

    public ServiceDataObjectRetriever(ServiceResponseFactory<R> serviceResponseFactory)
    {
        this.serviceResponseFactory = serviceResponseFactory;
    }

    /**
     * Performs an object retrieve on the specified RequestProcessor, defaulting to error cases if there is an issue.
     * The authorization of the ServiceRequest is passed on to the underlying DataObjectRequest.
     * @param serviceRequest The incoming ServiceRequest (with auth/cid)
     * @param requestProcessor The request processor to perform the retrieve with
     * @param objectId The id to retrieve
     * @param objectClass The class of the DefaultEndpointDataObject
     * @param <D> The type of the DefaultEndpointDataObject
     * @return ServiceDataRequestResult with either the data response for the object or a ServiceResponse due to error (error or object not found)
     */
    public <D extends DefaultEndpointDataObject> ServiceDataRequestResult<D, R> performObjectRetrieve(
        ServiceRequest serviceRequest, EndpointDataObjectRequestProcessor<D> requestProcessor,
        String objectId, Class<D> objectClass)
    {
        DataObjectRequest<D> dataObjectRequest = new DefaultDataObjectRequest<>(null, objectId, null);

        // just pass through from the original caller
        dataObjectRequest.setAuthorizationResponse(serviceRequest.getAuthorizationResponse());

        DataObjectResponse<D> dataObjectResponse = requestProcessor.handleGET(dataObjectRequest);
        ServiceDataRequestResult<D, R> dataRequestResult = new ServiceDataRequestResult<>();
        if(dataObjectResponse.isError())
        {
            dataRequestResult.setServiceResponse(serviceResponseFactory.createResponse(serviceRequest,
                dataObjectResponse.getErrorResponse(), String.format("%1$s %2$s retrieve failed", objectClass.getSimpleName(), objectId)));
        }
        else if(dataObjectResponse.getFirst() == null)
        {
            dataRequestResult.setServiceResponse(serviceResponseFactory.createResponse(serviceRequest,
                ErrorResponseFactory.objectNotFound(
                    String.format("%1$s %2$s not found", objectClass.getSimpleName(), objectId), serviceRequest.getCID()),
                null));
        }
        else
        {
            dataRequestResult.setDataObjectResponse(dataObjectResponse);
        }
        return dataRequestResult;
    }
}
