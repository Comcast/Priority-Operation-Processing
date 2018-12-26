package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.PersistentObjectConverter;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

/**
 */
public class DynamoDBConvertedObjectPersister<T extends IdentifiedObject> extends DynamoDBObjectPersister<T>
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
        return object;
    }

    protected DataObjectFeed<T> query(List<Query> queries) throws PersistenceException
    {
        DataObjectFeed<T> responseFeed = new DataObjectFeed<T>();
        try
        {
            List responseObjects;
            QueryExpression queryExpression = new QueryExpression(getTableIndexes(), queries);
            if(queryExpression.hasKey())
            {
                DynamoDBQueryExpression dynamoQueryExpression = queryExpression.forQuery();
                if(dynamoQueryExpression == null) return responseFeed;

                responseObjects = getDynamoDBMapper().query(converter.getPersistentObjectClass(), dynamoQueryExpression);
            }
            else
            {
                DynamoDBScanExpression dynamoScanExpression = queryExpression.forScan();

                responseObjects =  getDynamoDBMapper().scan(converter.getPersistentObjectClass(), dynamoScanExpression);
            }

            responseObjects.forEach(po -> responseFeed.add((T)converter.getDataObject(po)));
        }
        catch(AmazonDynamoDBException e)
        {
            throw new PersistenceException("Unable to run query", e);
        }

        return responseFeed;
    }
}
