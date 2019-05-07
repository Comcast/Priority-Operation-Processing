package com.theplatform.dfh.cp.endpoint.base.persistence;

import com.theplatform.dfh.cp.api.EndpointDataObject;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessorUtil;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

/**
 * Wrapper for object persistence so the time fields are updated on create/update.
 * @param <T> EndpointDataObject type
 */
public class EndpointDataObjectPersister<T extends EndpointDataObject> implements ObjectPersister<T>
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
        //RequestProcessorUtil.applyAddedTime(object);
        return objectPersister.persist(object);
    }

    @Override
    public T update(T object) throws PersistenceException
    {
        //RequestProcessorUtil.applyUpdatedTime(object);
        return objectPersister.update(object);
    }

    @Override
    public void delete(String identifier) throws PersistenceException
    {
        objectPersister.delete(identifier);
    }
}
