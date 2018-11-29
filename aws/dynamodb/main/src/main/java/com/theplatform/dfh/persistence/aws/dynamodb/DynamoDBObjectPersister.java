package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AmazonDynamoDBException;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.persistence.api.PersistentObjectConverter;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;

import java.io.IOException;
import java.util.*;

/**
 */
public class DynamoDBObjectPersister<T> implements ObjectPersister<T>
{
    protected static Logger logger = LoggerFactory.getLogger(DynamoDBObjectPersister.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private final String persistenceKeyFieldName;
    private final String tableName;
    private final AWSDynamoDBFactory AWSDynamoDBFactory;
    private final Class<T> dataObjectClass;

    private DynamoDBMapper dynamoDBMapper;
    private PersistentObjectConverter converter;
    private QueryExpression<T> queryExpression = new QueryExpression();

    public DynamoDBObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> dataObjectClass)
    {
        this.tableName = tableName;
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.AWSDynamoDBFactory = AWSDynamoDBFactory;
        this.dataObjectClass = dataObjectClass;

        AmazonDynamoDB client = getAWSDynamoDBFactory().getAmazonDynamoDB();
        DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder()
            .withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withTableNameOverride(TableNameOverride.withTableNameReplacement(tableName))
            .build();
        this.dynamoDBMapper = new DynamoDBMapper(client, mapperConfig);
    }

    public DynamoDBObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> dataObjectClass, PersistentObjectConverter converter)
    {
        this(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, dataObjectClass);
        this.converter = converter;
    }

    protected DynamoDBObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> dataObjectClass,
        DynamoDBMapper dynamoDBMapper)
    {
        this(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, dataObjectClass);
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public DataObjectFeed<T> retrieve(List<Query> queries) throws PersistenceException
    {
        return query(queries);
    }

    @Override
    public T retrieve(String identifier)
    {
        if (converter == null)
        {
            return dynamoDBMapper.load(dataObjectClass, identifier);
        }
        else
        {
            // todo fix this
            Object persistentObject = dynamoDBMapper.load(converter.getPersistentObjectClass(), identifier);
            return (T) converter.getDataObject(persistentObject);
        }
    }

    @Override
    public void delete(String identifier)
    {
        logger.info("Deleting {} instance with id {}.", dataObjectClass.getSimpleName(), identifier);
        AmazonDynamoDB client = AWSDynamoDBFactory.getAmazonDynamoDB();
        DeleteItemRequest deleteItemRequest = new DeleteItemRequest();
        deleteItemRequest.setKey(getKey(identifier));
        deleteItemRequest.setTableName(tableName);
        client.deleteItem(deleteItemRequest);
    }

    @Override
    public void persist(String identifier, T object)
    {
        logger.info("Persisting {} instance with id {}.", object.getClass().getSimpleName(), identifier);
        if (converter == null)
        {
            dynamoDBMapper.save(object);
        }
        else
        {
            Object persistentObject = converter.getPersistentObject(object);
            dynamoDBMapper.save(persistentObject);
        }
    }

    /**
     * Uses the persist method to simply overwrite the object.
     * @param identifier The key to update the item by
     * @param object The object to update
     */
    @Override
    public void update(String identifier, T object)
    {
        logger.info("Updating {} instance with id {}.", object.getClass().getSimpleName(), identifier);
        if (converter == null)
        {
            dynamoDBMapper.save(object);
        }
        else
        {
            Object persistentObject = converter.getPersistentObject(object);
            dynamoDBMapper.save(persistentObject);
        }
    }

    private DataObjectFeed<T> query(List<Query> queries) throws PersistenceException
    {
        DataObjectFeed<T> responseFeed = new DataObjectFeed<T>();
        DynamoDBQueryExpression dynamoQueryExpression = queryExpression.from(queries);
        if(dynamoQueryExpression == null) return responseFeed;
        try
        {
            List<T> responseObjects = dynamoDBMapper.query(dataObjectClass, dynamoQueryExpression);
            if(responseObjects == null || responseObjects.size() == 0) return responseFeed;

            responseFeed.addAll(responseObjects);
        }
        catch(AmazonDynamoDBException e)
        {
             throw new PersistenceException(String.format("Unable to run query for index {}, key {}, values {}", dynamoQueryExpression.getIndexName(),
                 dynamoQueryExpression.getKeyConditionExpression(), dynamoQueryExpression.getExpressionAttributeValues().toString()));
        }

        return responseFeed;
    }

    protected Map<String, AttributeValue> getKey(String identifier)
    {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(persistenceKeyFieldName, new AttributeValue(identifier));
        return key;
    }

    public String getPersistenceKeyFieldName()
    {
        return persistenceKeyFieldName;
    }

    public String getTableName()
    {
        return tableName;
    }

    public AWSDynamoDBFactory getAWSDynamoDBFactory()
    {
        return AWSDynamoDBFactory;
    }

    DynamoDBMapper getDynamoDBMapper()
    {
        return dynamoDBMapper;
    }

    void setDynamoDBMapper(DynamoDBMapper dynamoDBMapper)
    {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Class<T> getDataObjectClass()
    {
        return dataObjectClass;
    }

    protected String getJson(T object)
    {
        try
        {
            return objectMapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected T getObjectInstance(String json)
    {
        try
        {
            return objectMapper.readValue(json, getDataObjectClass());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
