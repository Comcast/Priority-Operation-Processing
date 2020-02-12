package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilterMap;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityMethod;
import com.theplatform.dfh.endpoint.api.*;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.ById;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.LinkedList;
import java.util.List;

/**
 * Basic implementation for request processing.
 * @param <T> The type of object to persist
 */
public class DataObjectRequestProcessor<T extends IdentifiedObject> implements RequestProcessor<DataObjectResponse<T>, DataObjectRequest<T>>
{
    protected ObjectPersister<T> objectPersister;
    private RequestValidator<DataObjectRequest<T>> requestValidator = new DataObjectValidator<>();
    private VisibilityFilterMap<T,DataObjectRequest<T>> visibilityFilterMap = new VisibilityFilterMap<>();
    public static final String OBJECT_NOT_FOUND_EXCEPTION = "Unable to get object by id %1$s";
    public static final String AUTHORIZATION_EXCEPTION = "You do not have permission to perform this action for customerId %1$s";
    public static final String UNABLE_TO_CREATE_EXCEPTION = "Unable to create object";
    public static final String UNABLE_TO_UPDATE_EXCEPTION = "Unable to update object by id %1$s";
    public static final String UNABLE_TO_DELETE_EXCEPTION = "Unable to delete object by id %1$s";

    public DataObjectRequestProcessor(ObjectPersister<T> objectPersister, DataObjectValidator<T, DataObjectRequest<T>> validator)
    {
        this.objectPersister = objectPersister;
    }
    public DataObjectRequestProcessor(ObjectPersister<T> objectPersister)
    {
        this.objectPersister = objectPersister;
    }
    /**
     * Handles the GET of an object
     * @return Response with the object, or an empty response if not found
     */
    @Override
    public DataObjectResponse<T> handleGET(DataObjectRequest<T> request)
    {
        if(getRequestValidator() != null) getRequestValidator().validateGET(request);

        DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
        try
        {
            VisibilityFilter<T, DataObjectRequest<T>> visibilityFilter = visibilityFilterMap.get(VisibilityMethod.GET);
            List<Query> queryList = new LinkedList<>();
            if(request.getQueries() != null)
                queryList.addAll(request.getQueries());

            if(request.getId() != null)
            {
                // if the id is specified
                queryList.add(new ById(request.getId()));
            }

            DataObjectFeed<T> feed = objectPersister.retrieve(queryList);
            if(feed == null)
                feed = new DataObjectFeed<>();
            List<T> filteredObjects = visibilityFilter.filterByVisible(request, feed.getAll());
            response.setCount(feed.getCount());
            response.addAll(filteredObjects);
        }
        catch(PersistenceException e)
        {
            ObjectNotFoundException objectNotFoundException = new ObjectNotFoundException(String.format(OBJECT_NOT_FOUND_EXCEPTION, request.getId()), e);
            response.setErrorResponse(ErrorResponseFactory.objectNotFound(objectNotFoundException, request.getCID()));
        }
        return response;
    }

    /**
     * Handles the POST of an object
     * @param request data object request
     * @return Response with the object
     */
    @Override
    public DataObjectResponse<T> handlePOST(DataObjectRequest<T> request)
    {
        if(getRequestValidator() != null) getRequestValidator().validatePOST(request);

        T dataObject = defaultFieldsOnCreate(request.getDataObject());
        DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
        VisibilityFilter<T, DataObjectRequest<T>> visibilityFilter = visibilityFilterMap.get(VisibilityMethod.POST);

        if(!visibilityFilter.isVisible(request, dataObject))
        {
            response.setErrorResponse(ErrorResponseFactory.unauthorized(String.format(AUTHORIZATION_EXCEPTION, dataObject.getCustomerId()), request.getCID()));
            return response;
        }

        try
        {
            T persistedObject = objectPersister.persist(dataObject);
            if(persistedObject == null) {
                Throwable throwable = new RuntimeException(UNABLE_TO_CREATE_EXCEPTION);
                response.setErrorResponse(ErrorResponseFactory.buildErrorResponse(throwable, 400, request.getCID()));
                return response;
            }
            response.add(persistedObject);
        }
        catch(PersistenceException e)
        {
            BadRequestException badRequestException = new BadRequestException(UNABLE_TO_CREATE_EXCEPTION, e);
            response.setErrorResponse(ErrorResponseFactory.badRequest(badRequestException, request.getCID()));
        }
        return response;
    }

    /**
     * Handles a PUT of an object
     * @param request holding the object to persist
     * @return Response with the object
     */
    @Override
    public DataObjectResponse<T> handlePUT(DataObjectRequest<T> request)
    {
        if(getRequestValidator() != null) getRequestValidator().validatePUT(request);

        T dataObjectToUpdate = request.getDataObject();
        String updatingCustomerId = dataObjectToUpdate.getCustomerId();
        DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
        try
        {
            // The object specified may not have the id set if the id is specified only in the url, set it now before moving on
            dataObjectToUpdate.setId(request.getId());
            VisibilityFilter<T, DataObjectRequest<T>> visibilityFilter = visibilityFilterMap.get(VisibilityMethod.PUT);
            //Get persisted object to verify visibility.
            T persistedDataObject = objectPersister.retrieve(dataObjectToUpdate.getId());
            if (persistedDataObject == null)
            {
                response.setErrorResponse(ErrorResponseFactory.objectNotFound(
                    new ObjectNotFoundException(String.format(OBJECT_NOT_FOUND_EXCEPTION, request.getId())), request.getCID()));
                return response;
            }
            if (! visibilityFilter.isVisible(request, persistedDataObject))
            {
                return new DefaultDataObjectResponse<>(ErrorResponseFactory.unauthorized(String.format(AUTHORIZATION_EXCEPTION, persistedDataObject.getCustomerId()),
                    request.getCID()));
            }

            //check the incoming customerID for visibility
            if (updatingCustomerId != null && !updatingCustomerId.equals(persistedDataObject.getCustomerId()) && !visibilityFilter.isVisible(request, dataObjectToUpdate))
            {
                return new DefaultDataObjectResponse<>(ErrorResponseFactory.unauthorized(String.format(AUTHORIZATION_EXCEPTION, dataObjectToUpdate.getCustomerId()),
                    request.getCID()));
            }

            // NOTE: the default update implementation is just a persist call
            response.add(objectPersister.update(dataObjectToUpdate));
            return response;
        }
        catch(PersistenceException e)
        {
            final String id = dataObjectToUpdate == null ? "UNKNOWN" : dataObjectToUpdate.getId();
            BadRequestException badRequestException = new BadRequestException(String.format(UNABLE_TO_UPDATE_EXCEPTION, id), e);
            response.setErrorResponse(ErrorResponseFactory.badRequest(badRequestException, request.getCID()));
        }
        return response;
    }

    /**
     * Handles the DELETE of an object
     * @param request The request holding the id of the object to delete
     * @return An empty response on success
     */
    @Override
    public DataObjectResponse<T> handleDELETE(DataObjectRequest<T> request)
    {
        if(getRequestValidator() != null) getRequestValidator().validateDELETE(request);
        try
        {
            DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
            if(request.getId() != null)
            {
                T object = objectPersister.retrieve(request.getId());
                if(object != null)
                {
                    if(! visibilityFilterMap.get(VisibilityMethod.DELETE).isVisible(request, object))
                        return new DefaultDataObjectResponse<>(
                            ErrorResponseFactory.unauthorized(
                                String.format(AUTHORIZATION_EXCEPTION, object.getCustomerId()), request.getCID()));

                    objectPersister.delete(request.getId());
                    response.add(object);
                }
            }
            return response;
        }
        catch(PersistenceException e)
        {
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.badRequest(
                new BadRequestException(String.format(UNABLE_TO_DELETE_EXCEPTION, request.getId()), e), request.getCID()));
        }
    }

    protected T defaultFieldsOnCreate(T object)
    {
        return object;
    }

    public DataObjectRequestProcessor<T> setVisibilityFilterMap(
        VisibilityFilterMap<T, DataObjectRequest<T>> map)
    {
        this.visibilityFilterMap = map;
        return this;
    }
    public DataObjectRequestProcessor<T> setVisibilityFilter(VisibilityMethod method,
        VisibilityFilter<T, DataObjectRequest<T>> filter)
    {
        this.visibilityFilterMap.put(method, filter);
        return this;
    }

    public RequestValidator<DataObjectRequest<T>> getRequestValidator()
    {
        return requestValidator;
    }

    public void setRequestValidator(RequestValidator<DataObjectRequest<T>> requestValidator)
    {
        this.requestValidator = requestValidator;
    }

    public ObjectPersister<T> getObjectPersister()
    {
        return objectPersister;
    }
}
