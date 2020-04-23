package com.comcast.pop.persistence.api;

/**
 */
public interface PersistentObjectConverter<T, S>
{
    Class<S> getPersistentObjectClass();
    Class<T> getDataObjectClass();

    S getPersistentObject(T dataObject);

    T getDataObject(S persistentObject);
}
