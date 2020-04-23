package com.comcast.pop.modules.queue.aws.sqs;

import com.comcast.pop.modules.queue.aws.sqs.processor.SQSQueueResult;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.pop.modules.queue.api.QueueResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class SQSItemQueueTestObject extends SQSItemQueueTest<QueueTestObject>
{
    @Override
    public QueueTestObject getTestObject()
    {
        return createQueueTestObject("1", "2");
    }

    @Override
    public Class getTestObjectClass()
    {
        return QueueTestObject.class;
    }

    @Override
    @DataProvider
    public Object[][] itemsDataProvider()
    {
        return new Object[][]
            {
                {new QueueTestObject[] {createQueueTestObject("1", "2")}},
                {new QueueTestObject[] {createQueueTestObject("1", "2"), createQueueTestObject("3", "4")}}
            };
    }

    private QueueTestObject createQueueTestObject(String id, String field)
    {
        QueueTestObject queueTestObject = new QueueTestObject();
        queueTestObject.setId(id);
        queueTestObject.setField(field);
        return queueTestObject;
    }

    @Test
    public void testAddJsonException() throws Exception
    {
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        sqsItemQueue.setObjectMapper(mockObjectMapper);
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(true);
        doReturn(sqsQueueResult).when(mockAddRequestProcessor).processRequest(any());
        doThrow(new JsonMappingException((Closeable)null, null)).when(mockObjectMapper).writeValueAsString(any());
        QueueResult<QueueTestObject> queueResult = sqsItemQueue.add(Collections.singletonList(getTestObject()));
        Assert.assertNotNull(queueResult);
        Assert.assertFalse(queueResult.isSuccessful());
    }

    @Test
    public void testPollJsonException() throws Exception
    {
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        sqsItemQueue.setObjectMapper(mockObjectMapper);
        SQSQueueResult sqsQueueResult = new SQSQueueResult().setSuccessful(true).setData(Collections.singletonList(""));
        doReturn(sqsQueueResult).when(mockPollRequestProcessor).processRequest(any());
        doThrow(new IOException()).when(mockObjectMapper).readValue(any(String.class), any(Class.class));
        QueueResult<QueueTestObject> queueResult = sqsItemQueue.poll();
        Assert.assertNotNull(queueResult);
        Assert.assertFalse(queueResult.isSuccessful());
    }
}
