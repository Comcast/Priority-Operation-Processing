package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.api.query.Query;
import com.theplatform.dfh.persistence.impl.QueryPredicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DynamoDBObjectPersister<T> implements ObjectPersister<T>
{
    protected static Logger logger = LoggerFactory.getLogger(DynamoDBObjectPersister.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    private final String persistenceKeyFieldName;
    private final String tableName;
    private final AWSDynamoDBFactory AWSDynamoDBFactory;
    private final Class<T> dataObjectClass;

    private DynamoDBMapper dynamoDBMapper;
    private TableIndexes tableIndexes;

    public DynamoDBObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> dataObjectClass, TableIndexes tableIndexes)
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
        this.tableIndexes = tableIndexes;
    }

    protected DynamoDBObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> dataObjectClass,
        DynamoDBMapper dynamoDBMapper, TableIndexes tableIndexes)
    {
        this(tableName, persistenceKeyFieldName, AWSDynamoDBFactory, dataObjectClass, tableIndexes);
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
        return dynamoDBMapper.load(dataObjectClass, identifier);
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
        dynamoDBMapper.save(object);
    }

    @Override
    public void update(String identifier, T object)
    {
        logger.info("Updating {} instance with id {}.", object.getClass().getSimpleName(), identifier);

        updateWithCondition(identifier, object);
    }

    protected DataObjectFeed<T> query(List<Query> queries) throws PersistenceException
    {
        DataObjectFeed<T> responseFeed = new DataObjectFeed<T>();
        try
        {
            List<T> responseObjects;
            // based on enum conversions this code will only work on very boring pojos
            QueryExpression queryExpression = new QueryExpression(tableIndexes, queries);
            if(queryExpression.hasKey())
            {
                DynamoDBQueryExpression dynamoQueryExpression = queryExpression.forQuery();
                if (dynamoQueryExpression == null)
                    return responseFeed;
                responseObjects = dynamoDBMapper.query(dataObjectClass, dynamoQueryExpression);
            }
            else
            {
                DynamoDBScanExpression dynamoScanExpression = queryExpression.forScan();
                responseObjects =  dynamoDBMapper.scan(dataObjectClass, dynamoScanExpression);
            }

            if(logger.isDebugEnabled())
                logger.debug("Total return object count {} ", responseObjects == null ? 0 : responseObjects.size());
            responseFeed.addAll(responseObjects);
        }
        catch(AmazonDynamoDBException e)
        {
             throw new PersistenceException("Unable to run query", e);
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

    protected void updateWithCondition(String identifier, Object object)
    {
        // only save if an object with that id exists
        Map<String, ExpectedAttributeValue> expected = new HashMap<>();
        expected.put("id", new ExpectedAttributeValue().withValue(new AttributeValue(identifier)));
        DynamoDBSaveExpression saveExpression = new DynamoDBSaveExpression();
        saveExpression.setExpected(expected);
        try
        {
            dynamoDBMapper.save(object, saveExpression);
        }
        catch (ConditionalCheckFailedException e)
        {
            throw new IllegalArgumentException("Could not update object.  No object with id " + identifier + " exists.", e);
        }
    }

    public TableIndexes getTableIndexes()
    {
        return tableIndexes;
    }
}
