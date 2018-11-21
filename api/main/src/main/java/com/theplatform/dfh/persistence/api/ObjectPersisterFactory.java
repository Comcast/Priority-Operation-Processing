package com.theplatform.dfh.persistence.api;

/**
 * Basic factory interface for object persistence
 * @param <T>
 */
public interface ObjectPersisterFactory<T>
{
    ObjectPersister<T> getObjectPersister(String containerName);
}
