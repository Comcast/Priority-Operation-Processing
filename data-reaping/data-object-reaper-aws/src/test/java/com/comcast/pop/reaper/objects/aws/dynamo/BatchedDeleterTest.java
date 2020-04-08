package com.comcast.pop.reaper.objects.aws.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteRequest;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.comcast.pop.modules.sync.util.ConsumerResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BatchedDeleterTest
{
    private BatchedDeleter batchedDeleter;
    private AmazonDynamoDB mockAmazonDynamoDB;
    private final String ID_FIELD = "id";
    private final String TABLE_NAME = "theTable";

    @BeforeMethod
    public void setup()
    {
        mockAmazonDynamoDB = mock(AmazonDynamoDB.class);
        batchedDeleter = new BatchedDeleter(mockAmazonDynamoDB, TABLE_NAME, ID_FIELD);
    }

    @DataProvider
    public Object[][] consumeNoDataProvider()
    {
        return new Object[][]
            {
                { null },
                { new ArrayList<String>() }
            };
    }

    @Test(dataProvider = "consumeNoDataProvider")
    public void testConsumeNoData(Collection<String> data)
    {
        ConsumerResult<String> consumerResult = batchedDeleter.consume(data, Instant.now().plusSeconds(60));
        Assert.assertEquals(consumerResult.getItemsConsumedCount(), 0);
        verify(mockAmazonDynamoDB, times(0)).batchWriteItem(any(BatchWriteItemRequest.class));
    }

    @DataProvider
    public Object[][] consumeProvider()
    {
        return new Object[][]
            {
                { createTestIds(1) },
                { createTestIds(BatchedDeleter.MAX_BATCH_WRITE_ITEM - 1) },
                { createTestIds(BatchedDeleter.MAX_BATCH_WRITE_ITEM) },
                { createTestIds(BatchedDeleter.MAX_BATCH_WRITE_ITEM + 1) },
                { createTestIds(1000) },
            };
    }

    @Test(dataProvider = "consumeProvider")
    public void testConsume(Collection<String> idsToDelete)
    {
        final int EXPECTED_DELETE_CALLS = (int)Math.ceil((double)idsToDelete.size() / (double)BatchedDeleter.MAX_BATCH_WRITE_ITEM);
        doReturn(createBatchWriteItemResult()).when(mockAmazonDynamoDB).batchWriteItem(any(BatchWriteItemRequest.class));
        ConsumerResult<String> consumerResult = batchedDeleter.consume(idsToDelete, Instant.now().plusSeconds(60));
        Assert.assertEquals(consumerResult.getItemsConsumedCount(), idsToDelete.size());
        verify(mockAmazonDynamoDB, times(EXPECTED_DELETE_CALLS)).batchWriteItem(any(BatchWriteItemRequest.class));
    }

    @Test
    public void testEndProcessingTime()
    {
        Collection<String> idsToDelete = createTestIds(50);
        doReturn(createBatchWriteItemResult()).when(mockAmazonDynamoDB).batchWriteItem(any(BatchWriteItemRequest.class));
        ConsumerResult<String> consumerResult = batchedDeleter.consume(idsToDelete, Instant.now().minusSeconds(60));
        Assert.assertEquals(consumerResult.getItemsConsumedCount(), BatchedDeleter.MAX_BATCH_WRITE_ITEM);
        verify(mockAmazonDynamoDB, times(1)).batchWriteItem(any(BatchWriteItemRequest.class));
    }

    @Test
    public void testConsumeWithDeleteUnprocessed()
    {
        Collection<String> idsToDelete = createTestIds(10);
        List<String> idsUnprocessedOnDelete = Collections.singletonList("1");
        doReturn(createBatchWriteItemResult(idsUnprocessedOnDelete)).when(mockAmazonDynamoDB).batchWriteItem(any(BatchWriteItemRequest.class));
        ConsumerResult<String> consumerResult = batchedDeleter.consume(idsToDelete, Instant.now().plusSeconds(60));
        Assert.assertEquals(consumerResult.getItemsConsumedCount(), idsToDelete.size() - idsUnprocessedOnDelete.size());
    }

    @Test
    public void testDynamoExceptionAsInterrupt()
    {
        Collection<String> idsToDelete = createTestIds(10);
        doThrow(new RuntimeException("FAIL")).when(mockAmazonDynamoDB).batchWriteItem(any(BatchWriteItemRequest.class));
        ConsumerResult<String> consumerResult = batchedDeleter.consume(idsToDelete, Instant.now().plusSeconds(60));
        Assert.assertTrue(consumerResult.isInterrupted());
    }

    private Collection<String> createTestIds(int count)
    {
        List<String> testIds = new LinkedList<>();
        IntStream.range(0, count)
            .forEach(i ->
                testIds.add(String.valueOf(i))
                );
        return testIds;
    }

    private BatchWriteItemResult createBatchWriteItemResult()
    {
        return createBatchWriteItemResult(null);
    }

    private BatchWriteItemResult createBatchWriteItemResult(List<String> unprocessedItems)
    {
        BatchWriteItemResult result = new BatchWriteItemResult();
        if(unprocessedItems != null && unprocessedItems.size() > 0)
            result.setUnprocessedItems(Collections.singletonMap(TABLE_NAME,
                unprocessedItems.stream()
                    .map(item ->
                        new WriteRequest().withDeleteRequest(
                            new DeleteRequest().addKeyEntry(ID_FIELD, new AttributeValue().withS(item))
                        )
                    ).collect(Collectors.toList()))
            );
        return result;
    }
}
