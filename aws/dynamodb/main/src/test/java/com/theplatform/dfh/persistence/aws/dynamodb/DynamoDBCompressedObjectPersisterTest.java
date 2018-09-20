package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.theplatform.dfh.compression.zlib.ZlibUtil;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class DynamoDBCompressedObjectPersisterTest
{
    private final String TEST_TABLE_NAME = "table.name";
    private final String TEST_ITEM_ID = "test.id";
    private final String TEST_VALUE = "test.value";

    private final String PERSISTENCE_KEY_FIELD = "key.field";

    private DynamoDBCompressedObjectPersister<TestTrackedObject> objectPersister;
    private AWSDynamoDBFactory mockAWSDynamoDBFactory;
    private AmazonDynamoDB mockAmazonDynamoDB;

    @BeforeMethod
    public void setup()
    {
        mockAmazonDynamoDB = mock(AmazonDynamoDB.class);
        mockAWSDynamoDBFactory = mock(AWSDynamoDBFactory.class);

        doReturn(mockAmazonDynamoDB).when(mockAWSDynamoDBFactory).getAmazonDynamoDB();

        objectPersister = new DynamoDBCompressedObjectPersister<>(
                TEST_TABLE_NAME,
                PERSISTENCE_KEY_FIELD,
                mockAWSDynamoDBFactory,
                TestTrackedObject.class);
        objectPersister.setZlibUtil(new ZlibUtil());
    }

    @Test
    public void testPersistAndRetrieve()
    {
        TestTrackedObject testTrackedObject = new TestTrackedObject(TEST_VALUE);

        final Map<String, AttributeValue> storedItem = new HashMap<>();

        // get the item that was put
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                PutItemRequest putItemRequest = (PutItemRequest)invocation.getArguments()[0];
                // dupe the input map
                putItemRequest.getItem().entrySet().forEach(item ->
                        storedItem.put(item.getKey(), item.getValue()));
                return null;
            }
        }).when(mockAmazonDynamoDB).putItem(any(PutItemRequest.class));

        // respond with the item that was put
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                GetItemResult getItemResult = new GetItemResult();
                getItemResult.setItem(storedItem);
                return getItemResult;
            }
        }).when(mockAmazonDynamoDB).getItem(any(GetItemRequest.class));

        objectPersister.persist(TEST_ITEM_ID, testTrackedObject);
        verify(mockAmazonDynamoDB, times(1)).putItem(any(PutItemRequest.class));
        validateAlwaysRequiredFields(storedItem);

        TestTrackedObject retrievedObject = objectPersister.retrieve(TEST_ITEM_ID);
        Assert.assertNotNull(retrievedObject);
        Assert.assertEquals(retrievedObject, testTrackedObject);
    }

    @Test
    public void testDelete()
    {
        objectPersister.delete(TEST_ITEM_ID);
        verify(mockAmazonDynamoDB, times(1)).deleteItem(any(DeleteItemRequest.class));
    }

    @Test
    public void testMissingRetrieve()
    {
        doReturn(new GetItemResult()).when(mockAmazonDynamoDB).getItem(any(GetItemRequest.class));
        Assert.assertNull(objectPersister.retrieve(TEST_ITEM_ID));
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testNullBlobRetrieve()
    {
        GetItemResult getItemResult = new GetItemResult();
        Map<String, AttributeValue> itemMap = new HashMap<>();
        getItemResult.setItem(itemMap);

        doReturn(getItemResult).when(mockAmazonDynamoDB).getItem(any(GetItemRequest.class));
        objectPersister.retrieve(TEST_ITEM_ID);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testNullBlobSizeRetrieve()
    {
        GetItemResult getItemResult = new GetItemResult();
        Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put(DynamoDBCompressedObjectPersister.DATA_BLOB,
                new AttributeValue().withB(ByteBuffer.wrap(new byte[1])));
        getItemResult.setItem(itemMap);
        doReturn(getItemResult).when(mockAmazonDynamoDB).getItem(any(GetItemRequest.class));
        objectPersister.retrieve(TEST_ITEM_ID);
    }

    private void validateAlwaysRequiredFields(Map<String, AttributeValue> map)
    {
        Assert.assertNotNull(map.get(DynamoDBCompressedObjectPersister.DATA_BLOB));

        // this is custom to the test
        Assert.assertNotNull(map.get(PERSISTENCE_KEY_FIELD));

    }
}
