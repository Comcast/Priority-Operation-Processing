package com.theplatform.dfh.persistence.aws.dynamodb;

import com.theplatform.dfh.object.api.IdGenerator;
import com.theplatform.dfh.object.api.UUIDGenerator;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 */
public class DynamoDBPersisterFactory<D extends IdentifiedObject> implements ObjectPersisterFactory<D>
{
    protected String persistenceKeyFieldName;
    private Class<D> dataObjectClass;
    private TableIndexes tableIndexes;
    private IdGenerator idGenerator = new UUIDGenerator();

    public DynamoDBPersisterFactory(String persistenceKeyFieldName, Class<D> dataObjectClass, TableIndexes tableIndexes)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.dataObjectClass = dataObjectClass;
        this.tableIndexes = tableIndexes;
    }

    @Override
    public ObjectPersister<D> getObjectPersister(String containerName)
    {
        DynamoDBObjectPersister<D> objectPersister = new DynamoDBObjectPersister<>(containerName, persistenceKeyFieldName,
           new AWSDynamoDBFactory(), dataObjectClass, tableIndexes);
        objectPersister.setIdGenerator(idGenerator);
        return objectPersister;
    }

    public DynamoDBPersisterFactory<D> setIdGenerator(IdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }
}