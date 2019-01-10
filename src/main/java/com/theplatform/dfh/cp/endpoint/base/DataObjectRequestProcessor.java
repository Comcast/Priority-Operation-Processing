package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;

/**
 * Basic implementation for request processing.
 * @param <T> The type of object to persist
 */
public class DataObjectRequestProcessor<T extends IdentifiedObject> implements RequestProcessor<DataObjectResponse<T>, DataObjectRequest<T>>
{
    protected ObjectPersister<T> objectPersister;

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
        try
        {
            DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
            if(request.getId() != null)
            {
                T object = objectPersister.retrieve(request.getId());
                response.add(object);
            }
            else
            {
                DataObjectFeed<T> feed = objectPersister.retrieve(request.getQueries());
                response.addAll(feed.getAll());
            }
            return response;
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to get object by id %1$s", request.getId()), e);
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
        try
        {
            T persistedObject = objectPersister.persist(request.getDataObject());
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
        // NOTE: the default update implementation is just a persist call
        try
        {
            objectPersister.update(request.getDataObject());
            DefaultDataObjectResponse<T> response = new DefaultDataObjectResponse<>();
            response.add(request.getDataObject());
            return response;

        }
        catch(PersistenceException e)
        {
            final String id = request.getDataObject() == null ? "UNKNOWN" : request.getDataObject().getId();
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
}
