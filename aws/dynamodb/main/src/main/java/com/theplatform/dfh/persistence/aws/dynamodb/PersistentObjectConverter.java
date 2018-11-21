package com.theplatform.dfh.persistence.aws.dynamodb;

/**
 * T = api object class
 * S = persistent object class
 */
public interface PersistentObjectConverter<T, S>
{
    S getPersistentObject(T dataObject);
    T getDataObject(S persistentObject);
}
