package com.theplatform.dfh.persistence.aws.dynamodb;

import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 * Wrapper factory for DynamoDBCompressedObjectPersister objects so we can swap in others later (and convenient unit testing)
 */
public class DynamoDBQueriableObjectPersisterFactory<T> implements ObjectPersisterFactory<T>
{
    protected String persistenceKeyFieldName;
    protected Class persistentObjectClass;

    public DynamoDBQueriableObjectPersisterFactory(String persistenceKeyFieldName, Class<T> clazz)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.persistentObjectClass = clazz;
    }

    @Override
    public ObjectPersister getObjectPersister(String containerName)
    {
        return new DynamoDBQueriableObjectPersister(containerName, persistentObjectClass);
    }
}
