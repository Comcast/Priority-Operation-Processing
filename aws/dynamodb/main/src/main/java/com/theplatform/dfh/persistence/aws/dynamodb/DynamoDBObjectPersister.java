package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.compression.zlib.ZlibUtil;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;

import java.io.IOException;

/**
 */
public class DynamoDBObjectPersister<T> implements ObjectPersister<T>
{
    protected static Logger logger = LoggerFactory.getLogger(DynamoDBObjectPersister.class);

    private ZlibUtil zlibUtil;
    private static ObjectMapper objectMapper = new ObjectMapper();

    private final String persistenceKeyFieldName;
    private final String tableName;
    private final AWSDynamoDBFactory AWSDynamoDBFactory;
    private final Class<T> clazz;

    private DynamoDBMapper dynamoDBMapper;

    public DynamoDBObjectPersister(String tableName,
        String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> clazz)
    {
        this.tableName = tableName;
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.AWSDynamoDBFactory = AWSDynamoDBFactory;
        this.clazz = clazz;
        this.zlibUtil = new ZlibUtil();

        AmazonDynamoDB client = getAWSDynamoDBFactory().getAmazonDynamoDB();
        DynamoDBMapperConfig mapperConfig = new DynamoDBMapperConfig.Builder()
            .withSaveBehavior(SaveBehavior.UPDATE_SKIP_NULL_ATTRIBUTES)
            .withTableNameOverride(TableNameOverride.withTableNameReplacement(tableName))
            .build();
        dynamoDBMapper = new DynamoDBMapper(client, mapperConfig);
    }

    @Override
    public T retrieve(String identifier)
    {
        return dynamoDBMapper.load(clazz, identifier);
    }

    @Override
    public void delete(String identifier)
    {
        logger.info("Deleting {} instance with id {}.", clazz.getSimpleName(), identifier);
    }

    @Override
    public void persist(String identifier, T object)
    {
        logger.info("Persisting {} instance with id {}.", object.getClass().getSimpleName(), identifier);
        dynamoDBMapper.save(object);
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
        dynamoDBMapper.save(object);
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

    public Class<T> getClazz()
    {
        return clazz;
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
            return objectMapper.readValue(json, getClazz());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
