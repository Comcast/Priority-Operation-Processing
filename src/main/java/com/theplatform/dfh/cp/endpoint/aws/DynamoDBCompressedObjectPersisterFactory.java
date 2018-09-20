package com.theplatform.dfh.cp.endpoint.aws;

import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersister;

/**
 * Wrapper factory for DynamoDBCompressedObjectPersister objects so we can swap in others later (and convenient unit testing)
 */
public class DynamoDBCompressedObjectPersisterFactory<T> implements ObjectPersisterFactory<T>
{
    protected String tableName;
    protected String persistenceKeyFieldName;
    protected Class persistentObjectClass;

    public DynamoDBCompressedObjectPersisterFactory(String tableName, String persistenceKeyFieldName, Class<T> clazz)
    {
        this.tableName = tableName;
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.persistentObjectClass = clazz;
    }

    @Override
    public ObjectPersister getObjectPersister()
    {
        return new DynamoDBCompressedObjectPersister<T>(tableName, persistenceKeyFieldName, new AWSDynamoDBFactory(), persistentObjectClass);
    }
}
