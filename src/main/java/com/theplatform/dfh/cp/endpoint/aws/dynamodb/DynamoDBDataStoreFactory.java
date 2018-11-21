package com.theplatform.dfh.cp.endpoint.aws.dynamodb;

import com.theplatform.dfh.cp.api.IdentifiedObject;
import com.theplatform.dfh.cp.endpoint.api.persistence.DataStore;
import com.theplatform.dfh.cp.endpoint.api.persistence.DataStoreFactory;
import com.theplatform.dfh.cp.endpoint.aws.dynamodb.DynamoDBDataStore;

public class DynamoDBDataStoreFactory<T extends IdentifiedObject> implements DataStoreFactory<T>
{
    protected String persistenceKeyFieldName;
    protected Class persistentObjectClass;

    public DynamoDBDataStoreFactory(String persistenceKeyFieldName, Class<T> clazz)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.persistentObjectClass = clazz;
    }

    @Override
    public DataStore getDataStore(String containerName)
    {
        return new DynamoDBDataStore<T>(containerName, persistentObjectClass);
    }
}
