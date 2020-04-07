package com.theplatform.dfh.cp.endpoint.util;

import com.comcast.pop.api.DefaultEndpointDataObject;
import com.comcast.pop.endpoint.base.EndpointDataObjectRequestProcessor;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ServiceResponse;
import com.comcast.pop.endpoint.api.auth.AuthorizationResponse;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;

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
        return performObjectRetrieve(dataObjectRequest, serviceRequest, requestProcessor, objectClass);
    }

    /**
     * Performs an object retrieve on the specified RequestProcessor, defaulting to error cases if there is an issue.
     * @param serviceRequest The incoming ServiceRequest (with auth/cid)
     * @param requestProcessor The request processor to perform the retrieve with
     * @param authorizationResponse The authorization response to use on the request
     * @param objectId The id to retrieve
     * @param objectClass The class of the DefaultEndpointDataObject
     * @param <D> The type of the DefaultEndpointDataObject
     * @return ServiceDataRequestResult with either the data response for the object or a ServiceResponse due to error (error or object not found)
     */
    public <D extends DefaultEndpointDataObject> ServiceDataRequestResult<D, R> performObjectRetrieve(
        ServiceRequest serviceRequest, EndpointDataObjectRequestProcessor<D> requestProcessor, AuthorizationResponse authorizationResponse,
        String objectId, Class<D> objectClass)
    {
        DataObjectRequest<D> dataObjectRequest = new DefaultDataObjectRequest<>(null, objectId, null);
        dataObjectRequest.setAuthorizationResponse(authorizationResponse);
        return performObjectRetrieve(dataObjectRequest, serviceRequest, requestProcessor, objectClass);
    }

    /**
     * Performs an object retrieve on the specified RequestProcessor, defaulting to error cases if there is an issue.
     * Authorization is controlled by the incoming dataObjectRequest
     * @param dataObjectRequest The DataObjectRequest to use to perform the lookup
     * @param serviceRequest The incoming ServiceRequest (with auth/cid)
     * @param requestProcessor The request processor to perform the retrieve with
     * @param objectClass The class of the DefaultEndpointDataObject
     * @param <D> The type of the DefaultEndpointDataObject
     * @return ServiceDataRequestResult with either the data response for the object or a ServiceResponse due to error (error or object not found)
     */
    public <D extends DefaultEndpointDataObject> ServiceDataRequestResult<D, R> performObjectRetrieve(
        DataObjectRequest<D> dataObjectRequest, ServiceRequest serviceRequest, EndpointDataObjectRequestProcessor<D> requestProcessor,
        Class<D> objectClass)
    {
        DataObjectResponse<D> dataObjectResponse = requestProcessor.handleGET(dataObjectRequest);
        ServiceDataRequestResult<D, R> dataRequestResult = new ServiceDataRequestResult<>();
        if(dataObjectResponse.isError())
        {
            dataRequestResult.setServiceResponse(serviceResponseFactory.createResponse(serviceRequest,
                dataObjectResponse.getErrorResponse(), String.format("%1$s %2$s retrieve failed", objectClass.getSimpleName(), dataObjectRequest.getId())));
        }
        else if(dataObjectResponse.getFirst() == null)
        {
            dataRequestResult.setServiceResponse(serviceResponseFactory.createResponse(serviceRequest,
                ErrorResponseFactory.objectNotFound(
                    String.format("%1$s %2$s not found", objectClass.getSimpleName(), dataObjectRequest.getId()), serviceRequest.getCID()),
                null));
        }
        else
        {
            dataRequestResult.setDataObjectResponse(dataObjectResponse);
        }
        return dataRequestResult;
    }
}
