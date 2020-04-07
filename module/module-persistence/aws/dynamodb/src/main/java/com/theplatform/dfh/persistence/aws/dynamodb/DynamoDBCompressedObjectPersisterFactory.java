package com.theplatform.dfh.persistence.aws.dynamodb;

import com.comcast.pop.object.api.IdGenerator;
import com.comcast.pop.object.api.UUIDGenerator;
import com.comcast.pop.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 * Wrapper factory for DynamoDBCompressedObjectPersister objects so we can swap in others later (and convenient unit testing)
 */
public class DynamoDBCompressedObjectPersisterFactory<T extends IdentifiedObject> implements ObjectPersisterFactory<T>
{
    protected String persistenceKeyFieldName;
    protected Class<T> persistentObjectClass;
    private IdGenerator idGenerator = new UUIDGenerator();

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

    public DynamoDBCompressedObjectPersisterFactory<T> setIdGenerator(IdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }
}
