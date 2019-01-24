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
public class DataObjectRequestProcessor<T extends IdentifiedObject> extends RequestProcessor<DataObjectResponse<T>, DataObjectRequest<T>>
{
    protected ObjectPersister<T> objectPersister;
    protected RequestValidator<DataObjectRequest<T>> validator = new DataObjectValidator<>();
    private VisibilityFilter<T, DataObjectRequest<T>> visibilityFilter = new CustomerVisibilityFilter<>();
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
     * @return Response with the object, or an empty response if not found
     */
    @Override
    protected DataObjectResponse<T> handleGET(DataObjectRequest<T> request)
    {
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
     * @return Response with the object
     */
    @Override
    protected DataObjectResponse<T> handlePOST(DataObjectRequest<T> request)
    {
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
     * @return Response with the object
     */
    @Override
    protected DataObjectResponse<T> handlePUT(DataObjectRequest<T> request)
    {
        T dataObjectToUpdate = request.getDataObject();
        String updatingCustomerId = dataObjectToUpdate.getCustomerId();
        try
        {
            if(dataObjectToUpdate != null)
            {
                //Get persisted object to verify visibility.
                T persistedDataObject = objectPersister.retrieve(dataObjectToUpdate.getId());
                if (persistedDataObject == null)
                    throw new ObjectNotFoundException(
                            String.format("Unable to get object by id %1$s", request.getId()));
                if (!visibilityFilter.isVisible(request, persistedDataObject))
                    throw new UnauthorizedException(String.format(AUTHORIZATION_EXCEPTION, persistedDataObject.getCustomerId()));

                //check the incoming customerID for visibility
                if (updatingCustomerId != null && !updatingCustomerId.equals(persistedDataObject.getCustomerId()) && !visibilityFilter.isVisible(request, dataObjectToUpdate))
                    throw new UnauthorizedException(String.format(AUTHORIZATION_EXCEPTION, dataObjectToUpdate.getCustomerId()));
                // NOTE: the default update implementation is just a persist call
                objectPersister.update(dataObjectToUpdate);
            }
            DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
            response.add(dataObjectToUpdate);
            return response;

        }
        catch(PersistenceException e)
        {
            final String id = dataObjectToUpdate == null ? "UNKNOWN" : dataObjectToUpdate.getId();
            throw new BadRequestException(String.format("Unable to update object by id %1$s", id), e);
        }
    }

    /**
     * Handles the DELETE of an object
     * @param request The request holding the id of the object to delete
     * @return An empty response on success
     */
    @Override
    protected DataObjectResponse<T> handleDELETE(DataObjectRequest<T> request)
    {
        try
        {
            if(request.getId() != null)
            {
                T object = objectPersister.retrieve(request.getId());
                if(object != null)
                {
                    if(!visibilityFilter.isVisible(request, object))
                        throw new UnauthorizedException(String.format(AUTHORIZATION_EXCEPTION, object.getCustomerId()));
                    else
                        objectPersister.delete(request.getId());

                }
            }
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
