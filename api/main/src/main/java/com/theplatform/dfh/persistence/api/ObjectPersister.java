package com.theplatform.dfh.persistence.api;

import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

public interface ObjectPersister<T extends IdentifiedObject>
{

    DataObjectFeed<T> retrieve(List<Query> queries) throws PersistenceException;

    /**
     * Retrieves the specified item from the table
     * @param identifier The key to use when looking up the item
     * @return The item if found, otherwise null
     */
    T retrieve(String identifier) throws PersistenceException;

    /**
     * Persists the item to the specified table by the given identifier
     * @param object The object to persist
     */
    T persist(T object) throws PersistenceException;

    /**
     * Updates the item specified in the table
     * @param object The object to update
     */
    T update(T object) throws PersistenceException;

    /**
     * Deletes the item from the specified table by the given idendifier
     * @param identifier The identifier of the item to remove
     */
    void delete(String identifier) throws PersistenceException;
}
