package com.theplatform.dfh.persistence.aws.dynamodb;

import com.comcast.pop.object.api.IdGenerator;
import com.comcast.pop.object.api.UUIDGenerator;
import com.comcast.pop.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;

/**
 */
public class DynamoDBPersisterFactory<D extends IdentifiedObject> implements ObjectPersisterFactory<D>
{
    protected String persistenceKeyFieldName;
    protected Class<D> dataObjectClass;
    protected TableIndexes tableIndexes;
    protected AWSDynamoDBFactory awsDynamoDBFactory;
    protected IdGenerator idGenerator = new UUIDGenerator();

    public DynamoDBPersisterFactory(String persistenceKeyFieldName, Class<D> dataObjectClass, TableIndexes tableIndexes)
    {
        this(persistenceKeyFieldName, dataObjectClass, tableIndexes, new AWSDynamoDBFactory());
    }

    public DynamoDBPersisterFactory(String persistenceKeyFieldName, Class<D> dataObjectClass, TableIndexes tableIndexes,
        AWSDynamoDBFactory awsDynamoDBFactory)
    {
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.dataObjectClass = dataObjectClass;
        this.tableIndexes = tableIndexes;
        this.awsDynamoDBFactory = awsDynamoDBFactory;
    }

    @Override
    public ObjectPersister<D> getObjectPersister(String containerName)
    {
        DynamoDBObjectPersister<D> objectPersister = new DynamoDBObjectPersister<>(containerName, persistenceKeyFieldName,
            awsDynamoDBFactory, dataObjectClass, tableIndexes);
        objectPersister.setIdGenerator(idGenerator);
        return objectPersister;
    }

    public DynamoDBPersisterFactory<D> setIdGenerator(IdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
        return this;
    }
}