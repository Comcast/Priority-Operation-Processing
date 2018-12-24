package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        try
        {
            return objectPersister.retrieve(id);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to get object by id %1$s", id), e);
        }
    }
    /**
     * Handles the GET of an object
     * @param queries Queries by fields
     * @return The object, or null if not found
     */
    public DataObjectFeed<T> handleGET(List<Query> queries)
    {
        try
        {
            return objectPersister.retrieve(queries);
        }
        catch(PersistenceException e)
        {
            final String queryString = queries.stream().map( Object::toString ).collect( Collectors.joining( "," ) );
            throw new BadRequestException(String.format("Unable to get object by queries %1$s", queryString), e);
        }
    }

    /**
     * Handles the POST of an object
     * @param objectToPersist The object to persist
     * @return Resulting id of the persisted object
     */
    public ObjectPersistResponse handlePOST(T objectToPersist)
    {
        String objectId = objectToPersist.getId() == null ? UUID.randomUUID().toString() : objectToPersist.getId();
        objectToPersist.setId(objectId);
        try
        {
            objectPersister.persist(objectToPersist);
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
    public void handlePUT(T objectToUpdate)
    {
        // NOTE: the default update implementation is just a persist call
        try
        {
            objectPersister.update(objectToUpdate);
        }
        catch(PersistenceException e)
        {
            final String id = objectToUpdate == null ? "UNKNOWN" : objectToUpdate.getId();
            throw new BadRequestException(String.format("Unable to update object by id %1$s", id), e);
        }
    }

    /**
     * Handles the DELETE of an object // TODO: should this return an object?
     * @param id The id of the object to delete
     */
    public void handleDelete(String id) {
        try
        {
            objectPersister.delete(id);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to delete object by id %1$s", id), e);
        }
    }
}
