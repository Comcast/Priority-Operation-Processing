package com.theplatform.dfh.persistence.api;

import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

public interface ObjectPersister<T>
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
     * @param identifier The key to store the item by
     * @param object The object to persist
     */
    void persist(String identifier, T object) throws PersistenceException;

    /**
     * Updates the item specified in the table
     * @param identifier The key to update the item by
     * @param object The object to update
     */
    void update(String identifier, T object) throws PersistenceException;

    /**
     * Deletes the item from the specified table by the given idendifier
     * @param identifier The identifier of the item to remove
     */
    void delete(String identifier) throws PersistenceException;
}
