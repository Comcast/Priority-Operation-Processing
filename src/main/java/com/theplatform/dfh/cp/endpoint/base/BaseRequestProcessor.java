package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.api.IdentifiedObject;
import com.theplatform.dfh.cp.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;

import java.util.UUID;

/**
 * Basic implementation for request processing.
 * @param <T> The type of object to persist
 */
public abstract class BaseRequestProcessor<T extends IdentifiedObject>
{
    protected ObjectPersister<T> objectPersister;

    public BaseRequestProcessor(ObjectPersister<T> objectPersister)
    {
        this.objectPersister = objectPersister;
    }

    /**
     * Handles the GET of an object
     * @param id id of the object to get
     * @return The object, or null if not found
     */
    public T handleGET(String id) throws BadRequestException
    {
        try
        {
            return objectPersister.retrieve(id);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to get object by id {}", id), e);
        }
    }

    /**
     * Handles the POST of an object
     * @param objectToPersist The object to persist
     * @return Resulting id of the persisted object
     */
    public ObjectPersistResponse handlePOST(T objectToPersist) throws BadRequestException
    {
        String objectId = UUID.randomUUID().toString();
        objectToPersist.setId(objectId);
        try
        {
            objectPersister.persist(objectId, objectToPersist);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException("Unable to create object", e);
        }
        return new ObjectPersistResponse(objectId);
    }

    /**
     * Handles a PUT of an object
     * @param objectToUpdate object to persist
     */
    public void handlePUT(T objectToUpdate) throws BadRequestException
    {
        // NOTE: the default update implementation is just a persist call
        try
        {
            objectPersister.update(objectToUpdate.getId(), objectToUpdate);
        }
        catch(PersistenceException e)
        {
            final String id = objectToUpdate == null ? "UNKNOWN" : objectToUpdate.getId();
            throw new BadRequestException(String.format("Unable to update object by id {}", id), e);
        }
    }

    /**
     * Handles the DELETE of an object // TODO: should this return an object?
     * @param id The id of the object to delete
     */
    public void handleDelete(String id) throws BadRequestException{
        try
        {
            objectPersister.delete(id);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to delete object by id {}", id), e);
        }
    }
}
