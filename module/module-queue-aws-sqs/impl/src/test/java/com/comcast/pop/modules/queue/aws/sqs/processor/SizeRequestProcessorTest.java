package com.comcast.pop.modules.queue.aws.sqs.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.comcast.pop.modules.queue.api.QueueResult;
import com.comcast.pop.modules.queue.aws.sqs.SQSRequestContext;
import com.comcast.pop.modules.queue.aws.sqs.api.QueueRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SizeRequestProcessorTest
{
    private SizeRequestProcessor sizeRequestProcessor;
    private AmazonSQS mockAmazonSQS;

    @BeforeMethod
    public void setup()
    {
        sizeRequestProcessor = new SizeRequestProcessor();
        mockAmazonSQS = mock(AmazonSQS.class);
    }

    @Test
    public void testMissingAttribute()
    {
        GetQueueAttributesResult getQueueAttributesResult = new GetQueueAttributesResult();
        getQueueAttributesResult.setAttributes(new HashMap<>());
        doReturn(getQueueAttributesResult).when(mockAmazonSQS).getQueueAttributes(any(GetQueueAttributesRequest.class));

        QueueResult result = sizeRequestProcessor.processRequest(
                new SQSRequestContext(mockAmazonSQS, new QueueRequest(), null));

        Assert.assertFalse(result.isSuccessful());
    }

    @Test
    public void testNormal()
    {
        final String EXPECTED_VALUE = "expected";

        GetQueueAttributesResult getQueueAttributesResult = new GetQueueAttributesResult();
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put(QueueAttributeName.ApproximateNumberOfMessages.name(), EXPECTED_VALUE);
        getQueueAttributesResult.setAttributes(attributeMap);
        doReturn(getQueueAttributesResult).when(mockAmazonSQS).getQueueAttributes(any(GetQueueAttributesRequest.class));

        QueueResult result = sizeRequestProcessor.processRequest(
                new SQSRequestContext(mockAmazonSQS, new QueueRequest(), null));

        Assert.assertTrue(result.isSuccessful());
        Assert.assertEquals(result.getMessage(), EXPECTED_VALUE);
    }
}
