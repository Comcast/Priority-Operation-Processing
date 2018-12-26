package com.theplatform.dfh.persistence.aws.dynamodb;

import com.theplatform.dfh.object.api.IDGenerator;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 * Wrapper factory for DynamoDBCompressedObjectPersister objects so we can swap in others later (and convenient unit testing)
 */
public class DynamoDBCompressedObjectPersisterFactory<T extends IdentifiedObject> implements ObjectPersisterFactory<T>
{
    protected String persistenceKeyFieldName;
    protected Class<T> persistentObjectClass;
    private IDGenerator idGenerator = new IDGenerator();

    public DynamoDBCompressedObjectPersisterFactory(String persistenceKeyFieldName, Class<T> clazz)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.persistentObjectClass = clazz;
    }

    @Override
    public ObjectPersister<T> getObjectPersister(String containerName)
    {
        DynamoDBCompressedObjectPersister<T> objectPersister = new DynamoDBCompressedObjectPersister<>(containerName, persistenceKeyFieldName, new AWSDynamoDBFactory(),
            persistentObjectClass);
        objectPersister.setIdGenerator(idGenerator);
        return objectPersister;
    }

    public DynamoDBCompressedObjectPersisterFactory<T> setIdGenerator(IDGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }
}
