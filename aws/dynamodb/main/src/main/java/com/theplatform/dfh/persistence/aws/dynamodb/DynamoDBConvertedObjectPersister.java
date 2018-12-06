package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.PersistentObjectConverter;
import com.theplatform.dfh.persistence.api.query.Query;
import java.util.List;

/**
 */
public class DynamoDBConvertedObjectPersister<T> extends DynamoDBObjectPersister<T>
{
    private PersistentObjectConverter converter;

    public DynamoDBConvertedObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> dataObjectClass, PersistentObjectConverter converter)
    {
        super(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, dataObjectClass);

        if (converter == null)
            throw new IllegalArgumentException("Must provide a PersistendObjectConverter.");
        this.converter = converter;
    }

    @Override
    public T retrieve(String identifier)
    {
        Object persistentObject = getDynamoDBMapper().load(converter.getPersistentObjectClass(), identifier);
        return (T) converter.getDataObject(persistentObject);
    }

    @Override
    public void persist(String identifier, T object)
    {
        logger.info("Persisting {} instance with id {}.", object.getClass().getSimpleName(), identifier);
        Object persistentObject = converter.getPersistentObject(object);
        getDynamoDBMapper().save(persistentObject);
    }

    @Override
    public void update(String identifier, T object)
    {
        logger.info("Updating {} instance with id {}.", object.getClass().getSimpleName(), identifier);

        Object persistentObject = converter.getPersistentObject(object);
        updateWithCondition(identifier, persistentObject);
    }

    protected DataObjectFeed<T> query(List<Query> queries) throws PersistenceException
    {
        DataObjectFeed<T> responseFeed = new DataObjectFeed<T>();
        DynamoDBQueryExpression dynamoQueryExpression = getQueryExpression().from(queries);
        if(dynamoQueryExpression == null) return responseFeed;
        try
        {
                List responseObjects = getDynamoDBMapper().query(converter.getPersistentObjectClass(), dynamoQueryExpression);
                if(responseObjects == null || responseObjects.size() == 0) return responseFeed;

                responseObjects.forEach(po -> responseFeed.add((T)converter.getDataObject(po)));
        }
        catch(AmazonDynamoDBException e)
        {
            throw new PersistenceException(String.format("Unable to run query for index %1$s, key %2$s, values %3$s", dynamoQueryExpression.getIndexName(),
                dynamoQueryExpression.getKeyConditionExpression(), dynamoQueryExpression.getExpressionAttributeValues().toString()));
        }

        return responseFeed;
    }
}
