package com.comcast.pop.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.pop.object.api.IdGenerator;
import com.comcast.pop.object.api.UUIDGenerator;
import com.comcast.pop.object.api.IdentifiedObject;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import com.comcast.pop.persistence.api.field.LimitField;
import com.comcast.pop.persistence.api.query.Query;
import com.comcast.pop.persistence.aws.dynamodb.retrieve.DynamoObjectRetrieverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;

import java.io.IOException;
import java.util.*;

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
    private DynamoObjectRetrieverFactory<T> dynamoObjectRetrieverFactory = new DynamoObjectRetrieverFactory<>();

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
        DataObjectFeed<T> responseFeed = new DataObjectFeed<>();
        List<T> responseObjects = performQuery(getDynamoObjectRetrieverFactory(), getDataObjectClass(), queries, responseFeed);
        if(responseObjects != null)
            responseFeed.addAll(responseObjects);
        return responseFeed;
    }

    /**
     * Performs the query for the specified objects
     * @param retrieverFactory The retriever factory to use to perform the query with
     * @param objectClass The class of the dynamo persisted object
     * @param queries The queries to perform
     * @param dataObjectFeed The feed to populate the item count with (if applicable)
     * @param <P> The type of object persisted in dynamo
     * @return null or list of P objects
     * @throws PersistenceException Exception from the dynamo query
     */
    protected <P extends IdentifiedObject> List<P> performQuery(
        DynamoObjectRetrieverFactory<P> retrieverFactory, Class<P> objectClass, List<Query> queries, DataObjectFeed dataObjectFeed
        ) throws PersistenceException
    {
        try
        {
            List<P> responseObjects = null;
            QueryExpression<P> queryExpression = new QueryExpression<>(getTableIndexes(), queries);
            if (queryExpression.hasCount())
            {
                DynamoDBQueryExpression<P> dynamoQueryExpression = queryExpression.forQuery();
                if (dynamoQueryExpression == null)
                    return responseObjects;
                final int count = getDynamoDBMapper().count(objectClass, dynamoQueryExpression);
                dataObjectFeed.setCount(count);
                return responseObjects;
            }
            responseObjects = retrieverFactory.createObjectRetriever(queryExpression, objectClass, getDynamoDBMapper())
                .retrieveObjects();
            if (logger.isDebugEnabled())
                logger.debug("DynamoDB total return object count {} ", responseObjects == null ? 0 : responseObjects.size());
            return responseObjects;
        }
        catch(AmazonDynamoDBException e)
        {
            throw new PersistenceException("DynamoDB unable to run query", e);
        }
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

    public DynamoObjectRetrieverFactory<T> getDynamoObjectRetrieverFactory()
    {
        return dynamoObjectRetrieverFactory;
    }

    public void setDynamoObjectRetrieverFactory(DynamoObjectRetrieverFactory<T> dynamoObjectRetrieverFactory)
    {
        this.dynamoObjectRetrieverFactory = dynamoObjectRetrieverFactory;
    }
}
