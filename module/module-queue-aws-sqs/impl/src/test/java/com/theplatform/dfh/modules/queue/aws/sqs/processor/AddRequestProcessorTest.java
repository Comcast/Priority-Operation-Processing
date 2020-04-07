package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSRequestContext;
import com.theplatform.dfh.modules.queue.aws.sqs.api.QueueRequest;
import com.comcast.pop.object.api.IdGenerator;
import com.comcast.pop.object.api.UUIDGenerator;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AddRequestProcessorTest
{
    private AddRequestProcessor addRequestProcessor;
    private MessageDigestUtil mockMessageDigestUtil;
    private AmazonSQS mockAmazonSQS;
    private IdGenerator uuidGenerator = new UUIDGenerator();

    @BeforeMethod
    public void setup()
    {
        addRequestProcessor = new AddRequestProcessor();
        addRequestProcessor.setIdGenerator(new TestUUIDGenerator());
        mockMessageDigestUtil = mock(MessageDigestUtil.class);
        mockAmazonSQS = mock(AmazonSQS.class);
        addRequestProcessor.setMessageDigestUtil(mockMessageDigestUtil);
    }

    @Test
    public void testMessageDigestException() throws Exception
    {
        doThrow(new NoSuchAlgorithmException()).when(mockMessageDigestUtil).getMD5String(anyString());
        QueueResult result = addRequestProcessor.processRequest(
                new SQSRequestContext(null, new QueueRequest().setData(Collections.singletonList("")), null));
        Assert.assertFalse(result.isSuccessful());
    }

    @Test
    public void testNormalRequest() throws Exception
    {
        final String GOOD_MD5 = "MD5-1";
        SendMessageBatchResult messageResult = new SendMessageBatchResult();
        messageResult.withSuccessful(new SendMessageBatchResultEntry().withMD5OfMessageBody(GOOD_MD5).withId("1"));
        doReturn(GOOD_MD5).when(mockMessageDigestUtil).getMD5String(anyString());
        doReturn(messageResult).when(mockAmazonSQS).sendMessageBatch(any());

        QueueResult result = addRequestProcessor.processRequest(
            new SQSRequestContext(mockAmazonSQS, new QueueRequest().setData(Collections.singletonList("")), null));

        verify(mockAmazonSQS, times(1)).sendMessageBatch(any(SendMessageBatchRequest.class));
        Assert.assertTrue(result.isSuccessful());
    }

    @Test
    public void testMD5Mismatch() throws Exception
    {
        final String GOOD_MD5 = "MD5-1";
        final String DIFFERENT_MD5 = "MD5-2";
        SendMessageBatchResult messageResult = new SendMessageBatchResult();
        messageResult.withSuccessful(new SendMessageBatchResultEntry().withMD5OfMessageBody(DIFFERENT_MD5).withId("1"));
        doReturn(GOOD_MD5).when(mockMessageDigestUtil).getMD5String(anyString());
        doReturn(messageResult).when(mockAmazonSQS).sendMessageBatch(any());

        QueueResult result = addRequestProcessor.processRequest(
                new SQSRequestContext(mockAmazonSQS, new QueueRequest().setData(Collections.singletonList("")), null));

        verify(mockAmazonSQS, times(1)).sendMessageBatch(any(SendMessageBatchRequest.class));
        Assert.assertFalse(result.isSuccessful());
    }

    private class TestUUIDGenerator implements IdGenerator
    {
        private int id = 0;
        @Override
        public String generate()
        {
            return Integer.toString(++id);
        }
    }
}
