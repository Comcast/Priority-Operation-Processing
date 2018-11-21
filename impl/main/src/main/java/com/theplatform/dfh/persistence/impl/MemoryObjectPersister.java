package com.theplatform.dfh.persistence.impl;

import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.query.Query;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryObjectPersister<T> implements ObjectPersister<T>
{
    private Map<String, T> objectPersistenceMap;

    public MemoryObjectPersister()
    {
        objectPersistenceMap = new HashMap<>();
    }

    @Override
    public DataObjectFeed<T> retrieve(List<Query> queries)
    {
        throw new NotImplementedException();
    }

    @Override
    public T retrieve(String identifier)
    {
        return objectPersistenceMap.get(identifier);
    }

    @Override
    public void persist(String identifier, T object)
    {
        objectPersistenceMap.put(identifier, object);
    }

    /**
     * Uses the persist method to simply overwrite the object.
     * @param identifier The key to update the item by
     * @param object The object to update
     */
    @Override
    public void update(String identifier, T object)
    {
        persist(identifier, object);
    }

    @Override
    public void delete(String identifier)
    {
        objectPersistenceMap.remove(identifier);
    }
}
