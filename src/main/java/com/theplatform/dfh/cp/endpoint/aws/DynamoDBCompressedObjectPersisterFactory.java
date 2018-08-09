package com.theplatform.dfh.cp.endpoint.aws;

import com.theplatform.dfh.schedule.persistence.api.ObjectPersister;
import com.theplatform.dfh.schedule.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.schedule.persistence.aws.dynamodb.DynamoDBCompressedObjectPersister;

/**
 * Wrapper factory for DynamoDBCompressedObjectPersister objects so we can swap in others later (and convenient unit testing)
 */
public class DynamoDBCompressedObjectPersisterFactory<T> implements ObjectPersisterFactory<T>
{
    private String tableName;
    private String persistenceKeyFieldName;
    private Class persistentObjectClass;

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
