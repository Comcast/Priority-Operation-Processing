package com.theplatform.dfh.cp.endpoint.aws;

import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersister;

/**
 * Wrapper factory for DynamoDBCompressedObjectPersister objects so we can swap in others later (and convenient unit testing)
 */
public class DynamoDBCompressedObjectPersisterFactory<T> implements ObjectPersisterFactory<T>
{
    protected String persistenceKeyFieldName;
    protected Class persistentObjectClass;

    public DynamoDBCompressedObjectPersisterFactory(String persistenceKeyFieldName, Class<T> clazz)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.persistentObjectClass = clazz;
    }

    @Override
    public ObjectPersister getObjectPersister(String containerName)
    {
        return new DynamoDBCompressedObjectPersister<T>(containerName, persistenceKeyFieldName, new AWSDynamoDBFactory(), persistentObjectClass);
    }
}
