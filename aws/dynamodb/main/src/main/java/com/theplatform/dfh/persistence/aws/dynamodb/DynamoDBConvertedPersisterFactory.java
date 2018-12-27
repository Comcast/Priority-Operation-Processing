package com.theplatform.dfh.persistence.aws.dynamodb;

import com.theplatform.dfh.object.api.IdGenerator;
import com.theplatform.dfh.object.api.UUIDGenerator;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 */
public class DynamoDBConvertedPersisterFactory<D extends IdentifiedObject> implements ObjectPersisterFactory<D>
{
    protected String persistenceKeyFieldName;
    private Class<D> dataObjectClass;
    private DynamoDBPersistentObjectConverter persistentObjectConverter;
    private TableIndexes tableIndexes;
    private IdGenerator idGenerator = new UUIDGenerator();

    public DynamoDBConvertedPersisterFactory(String persistenceKeyFieldName, Class<D> dataObjectClass,
        DynamoDBPersistentObjectConverter persistentObjectConverter, TableIndexes tableIndexes)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.dataObjectClass = dataObjectClass;
        this.persistentObjectConverter = persistentObjectConverter;
        this.tableIndexes = tableIndexes;
    }

    @Override
    public ObjectPersister<D> getObjectPersister(String containerName)
    {
        DynamoDBConvertedObjectPersister<D> objectPersister = new DynamoDBConvertedObjectPersister<>(containerName, persistenceKeyFieldName,
            new AWSDynamoDBFactory(), dataObjectClass, persistentObjectConverter, tableIndexes);
        objectPersister.setIdGenerator(idGenerator);
        return objectPersister;
    }

    public DynamoDBConvertedPersisterFactory<D> setIdGenerator(IdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }
}