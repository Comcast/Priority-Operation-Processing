package com.theplatform.dfh.persistence.aws.dynamodb;

import com.comcast.pop.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.PersistentObjectConverter;
import com.theplatform.dfh.persistence.api.query.Query;
import com.theplatform.dfh.persistence.aws.dynamodb.retrieve.DynamoObjectRetrieverFactory;

import java.util.List;

/**
 */
public class DynamoDBConvertedObjectPersister<T extends IdentifiedObject, S extends T> extends DynamoDBObjectPersister<T>
{
    private DynamoObjectRetrieverFactory<S> storedObjectRetrieverFactory = new DynamoObjectRetrieverFactory<>();
    private PersistentObjectConverter<T, S> converter;

    public DynamoDBConvertedObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> dataObjectClass, PersistentObjectConverter<T, S> converter, TableIndexes tableIndexes)
    {
        super(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, dataObjectClass, tableIndexes);

        if (converter == null)
            throw new IllegalArgumentException("Must provide a PersistendObjectConverter.");
        this.converter = converter;
    }

    @Override
    public T retrieve(String identifier)
    {
        S persistentObject = getDynamoDBMapper().load(converter.getPersistentObjectClass(), identifier);
        return converter.getDataObject(persistentObject);
    }

    @Override
    public T persist(T object)
    {
        if(object.getId() == null)
            object.setId(generateId());
        logger.info("Persisting {} instance with id {}.", object.getClass().getSimpleName(), object.getId());
        Object persistentObject = converter.getPersistentObject(object);
        getDynamoDBMapper().save(persistentObject);
        return object;
    }

    @Override
    public T update(T object)
    {
        logger.info("Updating {} instance with id {}.", object.getClass().getSimpleName(), object.getId());

        Object persistentObject = converter.getPersistentObject(object);
        updateWithCondition(object.getId(), persistentObject);
        return retrieve(object.getId());
    }

    /**
     * This is a custom override of the query functionality to support the concept of a persistent object vs. a client object
     * @param queries The queries to attempt
     * @return Data object feed of converted objects based on the results
     * @throws PersistenceException Exception from the dynamo query
     */
    @Override
    protected DataObjectFeed<T> query(List<Query> queries) throws PersistenceException
    {
        DataObjectFeed<T> responseFeed = new DataObjectFeed<>();
        List<S> responseObjects = performQuery(storedObjectRetrieverFactory, converter.getPersistentObjectClass(), queries, responseFeed);
        if(responseObjects != null)
            responseObjects.forEach(po -> responseFeed.add(converter.getDataObject(po)));
        return responseFeed;
    }

    public void setStoredObjectRetrieverFactory(DynamoObjectRetrieverFactory<S> storedObjectRetrieverFactory)
    {
        this.storedObjectRetrieverFactory = storedObjectRetrieverFactory;
    }
}
