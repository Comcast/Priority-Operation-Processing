package com.theplatform.dfh.persistence.aws.dynamodb;

/**
 * T = api object class
 * S = persistent object class
 */
public interface PersistentObjectConverter<T, S>
{
    public abstract S getPersistentObject(T dataObject);
    public abstract T getDataObject(S persistentObject);

    public Class<S> getPersistentObjectClass();
    public Class<T> getDataObjectClass();

}
