package com.theplatform.dfh.persistence.api;

/**
 */
public interface PersistentObjectConverter<T, S>
{
    public Class getPersistentObjectClass();
    public Class getDataObjectClass();

    public S getPersistentObject(T dataObject);

    public T getDataObject(S persistentObject);
}
