package com.theplatform.dfh.persistence.api;

public interface ObjectPersister<T>
{
    /**
     * Retrieves the specified item from the table
     * @param identifier The key to use when looking up the item
     * @return The item if found, otherwise null
     */
    T retrieve(String identifier);

    /**
     * Persists the item to the specified table by the given identifier
     * @param identifier The key to store the item by
     * @param object The object to persist
     */
    void persist(String identifier, T object);

    /**
     * Deletes the item from the specified table by the given idendifier
     * @param identifier The identifier of the item to remove
     */
    void delete(String identifier);
}
