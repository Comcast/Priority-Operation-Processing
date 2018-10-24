package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.compression.zlib.ZlibUtil;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Persists, retrieves, and deletes TrackedObjects in DynamoDB (compressed)
 */
public class DynamoDBCompressedObjectPersister<T> implements ObjectPersister<T>
{
    protected static Logger logger = LoggerFactory.getLogger(DynamoDBCompressedObjectPersister.class);

    private static ObjectMapper mapper = new ObjectMapper();
    public static final String DATA_BLOB = "dataBlob";

    private ZlibUtil zlibUtil;

    private final String persistenceKeyFieldName;
    private final String tableName;
    private final AWSDynamoDBFactory AWSDynamoDBFactory;
    private final Class<T> clazz;

    /**
     * Constructor for the persister
     * @param persistenceKeyFieldName The name of the field to store the identifier
     * @param AWSDynamoDBFactory The factory to create AmazonDynamoDB objects
     * @param clazz The class to persist / retrieve
     */
    public DynamoDBCompressedObjectPersister(String tableName,
            String persistenceKeyFieldName, AWSDynamoDBFactory AWSDynamoDBFactory, Class<T> clazz)
    {
        this.tableName = tableName;
        this.persistenceKeyFieldName = persistenceKeyFieldName;
        this.AWSDynamoDBFactory = AWSDynamoDBFactory;
        this.clazz = clazz;
        this.zlibUtil = new ZlibUtil();
    }

    public void setZlibUtil(ZlibUtil zlibUtil)
    {
        this.zlibUtil = zlibUtil;
    }

    @Override
    public T retrieve(String identifier)
    {
        AmazonDynamoDB client = AWSDynamoDBFactory.getAmazonDynamoDB();
        GetItemRequest gir = new GetItemRequest();
        gir.setConsistentRead(true);
        gir.setKey(getKey(identifier));
        gir.setTableName(tableName);
        GetItemResult giResult = client.getItem(gir);
        Map<String, AttributeValue> item = giResult.getItem();
        if (item == null)
        {
            return null;
        }
        AttributeValue dataBlob = item.get(DATA_BLOB);
        if (null == dataBlob)
        {
            throw new RuntimeException(
                    "No data found for \"" + DATA_BLOB + "\"");
        }
        else
        {
            String json = zlibUtil.inflateMe(dataBlob.getB().array());
            return getObjectInstance(json);
        }
    }

    @Override
    public void persist(String identifier, T object)
    {
        logger.info("Persisting {} instance.", object.getClass().getSimpleName());
        AmazonDynamoDB client = AWSDynamoDBFactory.getAmazonDynamoDB();
        PutItemRequest putItemRequest = getPutItemRequest(identifier, tableName, object);
        client.putItem(putItemRequest);
    }

    /**
     * Uses the persist method to simply overwrite the object.
     * @param identifier The key to update the item by
     * @param object The object to update
     */
    @Override
    public void update(String identifier, T object)
    {
        persist(identifier, object);
    }

    @Override
    public void delete(String identifier)
    {
        AmazonDynamoDB client = AWSDynamoDBFactory.getAmazonDynamoDB();
        DeleteItemRequest deleteItemRequest = new DeleteItemRequest();
        deleteItemRequest.setKey(getKey(identifier));
        deleteItemRequest.setTableName(tableName);
        client.deleteItem(deleteItemRequest);
    }

    protected Map<String, AttributeValue> getStringAttributeValueMap(String identifier, T object)
    {
        Map<String, AttributeValue> item = getKey(identifier);

        String objectJson = getJson(object);
        logger.debug("JSON: [{}]", objectJson);
        byte[] in;
        try
        {
            in = objectJson.getBytes("UTF-8");
            logger.debug("Inflated payload size {} in bytes, {} in kbytes", in.length, in.length/1024d);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }

        byte[] defalatedPayload = zlibUtil.deflateMe(in);
        logger.debug("Deflated payload size {} in bytes, {} in kbytes", defalatedPayload.length, defalatedPayload.length/1024d);
        item.put(DATA_BLOB, new AttributeValue().withB(ByteBuffer.wrap(defalatedPayload)));
        return item;
    }

    private PutItemRequest getPutItemRequest(String identifier, String tableName, T object)
    {
        PutItemRequest putItemRequest = new PutItemRequest();
        putItemRequest.setTableName(tableName);
        Map<String, AttributeValue> item = getStringAttributeValueMap(identifier, object);
        putItemRequest.setItem(item);
        return putItemRequest;
    }

    private String getJson(T object)
    {
        try
        {
            return mapper.writeValueAsString(object);
        }
        catch (JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected Map<String, AttributeValue> getKey(String identifier)
    {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put(persistenceKeyFieldName, new AttributeValue(identifier));
        return key;
    }

    private T getObjectInstance(String json)
    {
        try
        {
            return mapper.readValue(json, clazz);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected AWSDynamoDBFactory getAWSDynamoDBFactory()
    {
        return AWSDynamoDBFactory;
    }

    public String getPersistenceKeyFieldName()
    {
        return persistenceKeyFieldName;
    }

    public String getTableName()
    {
        return tableName;
    }
}
