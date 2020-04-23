package com.comcast.pop.modules.queue.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.comcast.pop.modules.queue.aws.sqs.processor.AddRequestProcessor;
import com.comcast.pop.modules.queue.aws.sqs.processor.PollRequestProcessor;
import com.comcast.pop.modules.queue.aws.sqs.processor.SQSQueueResult;
import com.comcast.pop.modules.queue.aws.sqs.processor.SizeRequestProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.pop.modules.queue.api.QueueResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public abstract class SQSItemQueueTest<T>
{
    private ObjectMapper objectMapper = new ObjectMapper();

    protected SQSItemQueue<T> sqsItemQueue;
    protected AmazonSQS mockAmazonSQS;
    protected PollRequestProcessor mockPollRequestProcessor;
    protected AddRequestProcessor mockAddRequestProcessor;
    protected SizeRequestProcessor mockSizeRequestProcessor;

    @BeforeMethod
    public void setup()
    {
        mockAddRequestProcessor = mock(AddRequestProcessor.class);
        mockPollRequestProcessor = mock(PollRequestProcessor.class);
        mockSizeRequestProcessor = mock(SizeRequestProcessor.class);
        mockAmazonSQS = mock(AmazonSQS.class);
        sqsItemQueue = new SQSItemQueue<>(mockAmazonSQS, "", getTestObjectClass());
        sqsItemQueue.setAddRequestProcessor(mockAddRequestProcessor);
        sqsItemQueue.setPollRequestProcessor(mockPollRequestProcessor);
        sqsItemQueue.setSizeRequestProcessor(mockSizeRequestProcessor);
    }

    public abstract T getTestObject();

    public abstract Class getTestObjectClass();

    @DataProvider
    public abstract Object[][] itemsDataProvider();

    @Test(dataProvider = "itemsDataProvider")
    public void testAddItems(T[] items)
    {
        Collection<T> itemsToAdd = Arrays.asList(items);
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(true);
        doReturn(sqsQueueResult).when(mockAddRequestProcessor).processRequest(any());
        QueueResult<T> queueResult = sqsItemQueue.add(itemsToAdd);
        Assert.assertNotNull(queueResult);
        Assert.assertTrue(queueResult.isSuccessful());
    }

    @Test
    public void testAddItemError()
    {
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(false);
        doReturn(sqsQueueResult).when(mockAddRequestProcessor).processRequest(any());
        QueueResult<T> queueResult = sqsItemQueue.add(Collections.singletonList(getTestObject()));
        Assert.assertNotNull(queueResult);
        Assert.assertFalse(queueResult.isSuccessful());
    }

    @Test
    public void testPollOneItem() throws Exception
    {
        final T EXPECTED_ITEM = getTestObject();
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(true).setData(Collections.singletonList(
            getTestObjectClass() == String.class ? (String)EXPECTED_ITEM : objectMapper.writeValueAsString(EXPECTED_ITEM)
        ));
        doReturn(sqsQueueResult).when(mockPollRequestProcessor).processRequest(any());
        QueueResult<T> queueResult = sqsItemQueue.poll();
        Assert.assertNotNull(queueResult);
        Assert.assertTrue(queueResult.isSuccessful());
        Assert.assertNotNull(queueResult.getData());
        Assert.assertEquals(queueResult.getData().size(), 1);
        Assert.assertTrue(queueResult.getData().contains(EXPECTED_ITEM));
    }

    @Test(dataProvider = "itemsDataProvider")
    public void testPollItems(T[] items) throws Exception
    {
        Collection<T> itemsToAdd = Arrays.asList(items);
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(true).setData(
            getTestObjectClass() == String.class
            ? (Collection<String>)itemsToAdd
            : itemsToAdd.stream().map(i ->
                {
                    try { return objectMapper.writeValueAsString(i); }
                    catch(Exception e){ throw new RuntimeException(e); }
                }
            ).collect(Collectors.toList())
        );
        doReturn(sqsQueueResult).when(mockPollRequestProcessor).processRequest(any());
        QueueResult<T> queueResult = sqsItemQueue.poll(items.length);
        Assert.assertNotNull(queueResult);
        Assert.assertTrue(queueResult.isSuccessful());
        Assert.assertNotNull(queueResult.getData());
        Assert.assertEquals(queueResult.getData().size(), items.length);
        Assert.assertTrue(queueResult.getData().containsAll(itemsToAdd));
    }

    @Test
    public void testPollError()
    {
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(false);
        doReturn(sqsQueueResult).when(mockPollRequestProcessor).processRequest(any());
        QueueResult<T> queueResult = sqsItemQueue.poll();
        Assert.assertNotNull(queueResult);
        Assert.assertFalse(queueResult.isSuccessful());
    }

    @Test
    public void testSize()
    {
        final String EXPECTED_SIZE = "5";
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(true).setMessage(EXPECTED_SIZE);
        doReturn(sqsQueueResult).when(mockSizeRequestProcessor).processRequest(any());
        QueueResult<T> queueResult = sqsItemQueue.size();
        Assert.assertNotNull(queueResult);
        Assert.assertEquals(queueResult.getMessage(), EXPECTED_SIZE);
    }

    @Test
    public void testSizeError()
    {
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(false);
        doReturn(sqsQueueResult).when(mockSizeRequestProcessor).processRequest(any());
        QueueResult<T> queueResult = sqsItemQueue.size();
        Assert.assertNotNull(queueResult);
        Assert.assertFalse(queueResult.isSuccessful());
    }
}
