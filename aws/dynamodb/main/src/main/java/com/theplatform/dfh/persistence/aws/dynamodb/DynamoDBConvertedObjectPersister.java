package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.PersistentObjectConverter;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.Collections;
import java.util.List;

/**
 */
public class DynamoDBConvertedObjectPersister<T> extends DynamoDBObjectPersister<T>
{
    private PersistentObjectConverter converter;

    public DynamoDBConvertedObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> dataObjectClass, PersistentObjectConverter converter, TableIndexes tableIndexes)
    {
        super(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, dataObjectClass, tableIndexes);

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
        DynamoDBQueryExpression dynamoQueryExpression = getQueryExpression().forQuery(queries);
        if(dynamoQueryExpression == null) return responseFeed;
        try
        {
            List responseObjects;
            if(queries != null && queries.size() > 0)
            {
                responseObjects = getDynamoDBMapper().query(converter.getPersistentObjectClass(), dynamoQueryExpression);
            }
            else
            {
                queries = Collections.singletonList(new Query(new LimitField(), LimitField.defaultValue()));
                DynamoDBScanExpression dynamoScanExpression = getQueryExpression().forScan(queries);

                responseObjects =  getDynamoDBMapper().scan(converter.getPersistentObjectClass(), dynamoScanExpression);
            }

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
