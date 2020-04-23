package com.comcast.pop.endpoint.base.persistence;

import com.comcast.pop.api.DefaultEndpointDataObject;
import com.comcast.pop.endpoint.base.RequestProcessorUtil;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import com.comcast.pop.persistence.api.query.Query;

import java.util.List;

/**
 * Wrapper for object persistence so the time fields are updated on create/update.
 * @param <T> DefaultEndpointDataObject type
 */
public class EndpointDataObjectPersister<T extends DefaultEndpointDataObject> implements ObjectPersister<T>
{
    private ObjectPersister<T> objectPersister;

    public EndpointDataObjectPersister(ObjectPersister<T> objectPersister)
    {
        this.objectPersister = objectPersister;
    }

    @Override
    public DataObjectFeed<T> retrieve(List<Query> queries) throws PersistenceException
    {
        return objectPersister.retrieve(queries);
    }

    @Override
    public T retrieve(String identifier) throws PersistenceException
    {
        return objectPersister.retrieve(identifier);
    }

    @Override
    public T persist(T object) throws PersistenceException
    {
        RequestProcessorUtil.applyAddedTime(object);
        return objectPersister.persist(object);
    }

    @Override
    public T update(T object) throws PersistenceException
    {
        RequestProcessorUtil.applyUpdatedTime(object);
        return objectPersister.update(object);
    }

    @Override
    public void delete(String identifier) throws PersistenceException
    {
        objectPersister.delete(identifier);
    }
}
