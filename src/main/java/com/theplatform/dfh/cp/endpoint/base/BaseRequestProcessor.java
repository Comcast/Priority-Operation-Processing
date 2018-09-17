package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.api.IdentifiedObject;
import com.theplatform.dfh.cp.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.schedule.persistence.api.ObjectPersister;

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
    public T handleGET(String id)
    {
        return objectPersister.retrieve(id);
    }

    /**
     * Handles the POST of an object
     * @param objectToPersist The object to persist
     * @return Resulting id of the persisted object
     */
    public ObjectPersistResponse handlePOST(T objectToPersist)
    {
        String objectId = UUID.randomUUID().toString();
        objectToPersist.setId(objectId);
        objectPersister.persist(objectId, objectToPersist);
        return new ObjectPersistResponse(objectId);
    }

    /**
     * Handles a PUT of an object
     * @param objectToUpdate object to persist
     */
    public void handlePUT(T objectToUpdate)
    {
        // TODO: this is just a delete and overwrite (we're not doing a proper update with dynamodb yet)... not a join/append
        //objectPersister.delete(objectToUpdate.getId());
        objectPersister.persist(objectToUpdate.getId(), objectToUpdate);
    }

    /**
     * Handles the DELETE of an object // TODO: should this return an object?
     * @param id The id of the object to delete
     */
    public void handleDelete(String id) { objectPersister.delete(id); }
}
