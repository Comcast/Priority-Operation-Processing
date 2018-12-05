package com.theplatform.dfh.persistence.aws.dynamodb;

import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 */
public class DynamoDBConvertedPersisterFactory<D> implements ObjectPersisterFactory<D>
{
    protected String persistenceKeyFieldName;
    private Class dataObjectClass;
    private DynamoDBPersistentObjectConverter persistentObjectConverter;

    public DynamoDBConvertedPersisterFactory(String persistenceKeyFieldName, Class dataObjectClass,
        DynamoDBPersistentObjectConverter persistentObjectConverter)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.dataObjectClass = dataObjectClass;
        this.persistentObjectConverter = persistentObjectConverter;
    }

    @Override
    public ObjectPersister<D> getObjectPersister(String containerName)
    {
        return new DynamoDBConvertedObjectPersister<D>(containerName, persistenceKeyFieldName, new AWSDynamoDBFactory(), dataObjectClass, persistentObjectConverter);
    }
}