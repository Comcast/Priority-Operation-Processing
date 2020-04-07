package com.theplatform.dfh.persistence.aws.dynamodb;

import com.comcast.pop.object.api.IdGenerator;
import com.comcast.pop.object.api.UUIDGenerator;
import com.comcast.pop.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 */
public class DynamoDBConvertedPersisterFactory<T extends IdentifiedObject, S extends T> implements ObjectPersisterFactory<T>
{
    protected String persistenceKeyFieldName;
    private Class<T> dataObjectClass;
    private DynamoDBPersistentObjectConverter<T, S> persistentObjectConverter;
    private TableIndexes tableIndexes;
    private IdGenerator idGenerator = new UUIDGenerator();

    public DynamoDBConvertedPersisterFactory(String persistenceKeyFieldName, Class<T> dataObjectClass,
        DynamoDBPersistentObjectConverter<T, S> persistentObjectConverter, TableIndexes tableIndexes)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.dataObjectClass = dataObjectClass;
        this.persistentObjectConverter = persistentObjectConverter;
        this.tableIndexes = tableIndexes;
    }

    @Override
    public ObjectPersister<T> getObjectPersister(String containerName)
    {
        DynamoDBConvertedObjectPersister<T, S> objectPersister = new DynamoDBConvertedObjectPersister<>(containerName, persistenceKeyFieldName,
            new AWSDynamoDBFactory(), dataObjectClass, persistentObjectConverter, tableIndexes);
        objectPersister.setIdGenerator(idGenerator);
        return objectPersister;
    }

    public DynamoDBConvertedPersisterFactory<T, S> setIdGenerator(IdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }
}