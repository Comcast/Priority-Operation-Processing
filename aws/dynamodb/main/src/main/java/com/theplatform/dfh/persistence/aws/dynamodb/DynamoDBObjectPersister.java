package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.object.api.IdGenerator;
import com.theplatform.dfh.object.api.UUIDGenerator;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.field.LimitField;
import com.theplatform.dfh.persistence.api.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DynamoDBObjectPersister<T extends IdentifiedObject> implements ObjectPersister<T>
{
    protected static Logger logger = LoggerFactory.getLogger(DynamoDBObjectPersister.class);

    private static final LimitField limitField = new LimitField();
    private static ObjectMapper objectMapper = new ObjectMapper();

    private final String persistenceKeyFieldName;
    private final String tableName;
    private final AWSDynamoDBFactory AWSDynamoDBFactory;
    private final Class<T> dataObjectClass;
    private IdGenerator idGenerator = new UUIDGenerator();

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
    public T persist(T object)
    {
        if(object.getId() == null)
            object.setId(generateId());
        logger.info("Persisting {} instance with id {}.", object.getClass().getSimpleName(), object.getId());
        dynamoDBMapper.save(object);
        return object;
    }

    @Override
    public T update(T object)
    {
        logger.info("Updating {} instance with id {}.", object.getClass().getSimpleName(), object.getId());

        updateWithCondition(object.getId(), object);

        return retrieve(object.getId());
    }

    protected DataObjectFeed<T> query(List<Query> queries) throws PersistenceException
    {
        DataObjectFeed<T> responseFeed = new DataObjectFeed<T>();
        try
        {
            List<T> responseObjects;
            // based on enum conversions this code will only work on very boring pojos
            QueryExpression<T> queryExpression = new QueryExpression<>(tableIndexes, queries);
            if (queryExpression.hasCount())
            {
                DynamoDBQueryExpression<T> dynamoQueryExpression = queryExpression.forQuery();
                if (dynamoQueryExpression == null)
                    return responseFeed;
                final int count = dynamoDBMapper.count(dataObjectClass, dynamoQueryExpression);
                responseFeed.setCount(count);
                return responseFeed;
            }
            else if (queryExpression.hasKey())
            {
                DynamoDBQueryExpression<T> dynamoQueryExpression = queryExpression.forQuery();
                if (dynamoQueryExpression == null)
                    return responseFeed;
                responseObjects = dynamoDBMapper.query(dataObjectClass, dynamoQueryExpression);
            }
            else
            {
                DynamoDBScanExpression dynamoScanExpression = queryExpression.forScan();
                responseObjects = dynamoDBMapper.scan(dataObjectClass, dynamoScanExpression);
            }

            if (logger.isDebugEnabled())
                logger.debug("DynamoDB total return object count {} ", responseObjects == null ? 0 : responseObjects.size());

            final Integer limit = queryExpression.getLimit();
            if (limit != null && responseObjects != null)
            {
                //DynamoDB just returns the pointers for the items, we need to restrict our return set.
                logger.info("DynamoDB limiting return set to {}", limit);
                responseFeed.addAll(responseObjects.stream().limit(limit).collect(Collectors.toList()));
            }
            else
            {
                responseFeed.addAll(responseObjects);
            }
        }
        catch(AmazonDynamoDBException e)
        {
             throw new PersistenceException("DynamoDB unable to run query", e);
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

    protected String generateId()
    {
        return idGenerator.generate();
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

    public void setIdGenerator(IdGenerator idGenerator)
    {
        this.idGenerator = idGenerator;
    }
}
