package com.theplatform.dfh.persistence.aws.dynamodb;

import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 */
public class DynamoDBPersisterFactory<D> implements ObjectPersisterFactory<D>
{
    protected String persistenceKeyFieldName;
    private Class dataObjectClass;

    public DynamoDBPersisterFactory(String persistenceKeyFieldName, Class dataObjectClass)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.dataObjectClass = dataObjectClass;
    }

    @Override
    public ObjectPersister<D> getObjectPersister(String containerName)
    {
       return new DynamoDBObjectPersister<D>(containerName, persistenceKeyFieldName, new AWSDynamoDBFactory(), dataObjectClass);
    }
}