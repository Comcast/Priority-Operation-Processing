package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.endpoint.base.validation.DataObjectValidator;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.CustomerVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilter;
import com.theplatform.dfh.endpoint.api.*;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;

import java.util.List;

/**
 * Basic implementation for request processing.
 * @param <T> The type of object to persist
 */
public class DataObjectRequestProcessor<T extends IdentifiedObject> implements RequestProcessor<DataObjectResponse<T>, DataObjectRequest<T>>
{
    protected ObjectPersister<T> objectPersister;
    protected RequestValidator<DataObjectRequest<T>> validator = new DataObjectValidator<>();
    private VisibilityFilter<T, DataObjectRequest<T>> visibilityFilter = new CustomerVisibilityFilter<T, DataObjectRequest<T>>();
    private static final String AUTHORIZATION_EXCEPTION = "You do not have permission to perform this action for customerId %1$s";

    public DataObjectRequestProcessor(ObjectPersister<T> objectPersister, DataObjectValidator validator)
    {
        this.validator = validator;
        this.objectPersister = objectPersister;
    }
    public DataObjectRequestProcessor(ObjectPersister<T> objectPersister)
    {
        this.objectPersister = objectPersister;
    }
    /**
     * Handles the GET of an object
     * @return The object, or null if not found
     */
    @Override
    public DataObjectResponse<T> handleGET(DataObjectRequest<T> request)
    {
        validator.validateGET(request);
        try
        {
            DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
            if(request.getId() != null)
            {
                T object = objectPersister.retrieve(request.getId());
                if(object == null)
                    throw new ObjectNotFoundException(String.format("Unable to get object by id %1$s", request.getId()));
                if(visibilityFilter.isVisible(request, object))
                    response.add(object);
            }
            else
            {
                DataObjectFeed<T> feed = objectPersister.retrieve(request.getQueries());
                List<T> filteredObjects = visibilityFilter.filterByVisible(request, feed.getAll());
                response.addAll(filteredObjects);
            }
            return response;
        }
        catch(PersistenceException e)
        {
            throw new ObjectNotFoundException(String.format("Unable to get object by id %1$s", request.getId()), e);
        }
    }

    /**
     * Handles the POST of an object
     * @param request data object request
     * @return Resulting id of the persisted object
     */
    @Override
    public DataObjectResponse<T> handlePOST(DataObjectRequest<T> request)
    {
        validator.validatePOST(request);
        T dataObject = request.getDataObject();
        if(!visibilityFilter.isVisible(request, dataObject))
           throw new UnauthorizedException(String.format(AUTHORIZATION_EXCEPTION, dataObject.getCustomerId()));
        try
        {
            T persistedObject = objectPersister.persist(dataObject);
            if(persistedObject == null) throw new RuntimeException("Unable to create object " +dataObject.getId());
            DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
            response.add(persistedObject);
            return response;
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException("Unable to create object", e);
        }
    }

    /**
     * Handles a PUT of an object
     * @param request holding the object to persist
     */
    @Override
    public DataObjectResponse<T> handlePUT(DataObjectRequest<T> request)
    {
        validator.validatePUT(request);
        T dataObject = request.getDataObject();
        if(!visibilityFilter.isVisible(request, dataObject))
            throw new UnauthorizedException(String.format(AUTHORIZATION_EXCEPTION, dataObject.getCustomerId()));
        // NOTE: the default update implementation is just a persist call
        try
        {
            objectPersister.update(dataObject);
            DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
            response.add(dataObject);
            return response;

        }
        catch(PersistenceException e)
        {
            final String id = dataObject == null ? "UNKNOWN" : dataObject.getId();
            throw new BadRequestException(String.format("Unable to update object by id %1$s", id), e);
        }
    }

    /**
     * Handles the DELETE of an object
     * @param request The request holding the id of the object to delete
     */
    @Override
    public DataObjectResponse<T> handleDELETE(DataObjectRequest<T> request)
    {
        validator.validateDELETE(request);
        T dataObject = request.getDataObject();
        if(!visibilityFilter.isVisible(request, dataObject))
            throw new UnauthorizedException(String.format(AUTHORIZATION_EXCEPTION, dataObject.getCustomerId()));
        try
        {
            objectPersister.delete(request.getId());
            return new DefaultDataObjectResponse<>();
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to delete object by id %1$s", request.getId()), e);
        }
    }

    public void setValidator(RequestValidator<DataObjectRequest<T>> validator)
    {
        this.validator = validator;
    }

    @Override
    public RequestValidator<DataObjectRequest<T>> getRequestValidator()
    {
        return new DataObjectValidator<>();
    }
}
