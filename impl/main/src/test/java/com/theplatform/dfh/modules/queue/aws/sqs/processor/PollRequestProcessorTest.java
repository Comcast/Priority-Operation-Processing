package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSRequestContext;
import com.theplatform.dfh.modules.queue.aws.sqs.api.QueueRequest;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PollRequestProcessorTest
{
    private PollRequestProcessor pollRequestProcessor;
    private AmazonSQS mockAmazonSQS;

    @BeforeMethod
    public void setup()
    {
        pollRequestProcessor = new PollRequestProcessor();
        mockAmazonSQS = mock(AmazonSQS.class);
    }

    @Test
    public void testNoResults()
    {
        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();
        receiveMessageResult.setMessages(new ArrayList<>());
        doReturn(receiveMessageResult).when(mockAmazonSQS).receiveMessage(any(ReceiveMessageRequest.class));

        QueueResult result = pollRequestProcessor.processRequest(
                new SQSRequestContext(mockAmazonSQS, new QueueRequest(), null));

        verify(mockAmazonSQS, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
        Assert.assertTrue(result.isSuccessful());
        Assert.assertNull(result.getData());
    }

    @Test
    public void testOneResult()
    {
        final String EXPECTED_BODY = "expected";
        ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult();

        List<Message> messageList = new ArrayList<>();
        Message expectedMessage = new Message();
        expectedMessage.setBody(EXPECTED_BODY);
        messageList.add(expectedMessage);
        receiveMessageResult.setMessages(messageList);

        doReturn(receiveMessageResult).when(mockAmazonSQS).receiveMessage(any(ReceiveMessageRequest.class));

        QueueResult result = pollRequestProcessor.processRequest(
                new SQSRequestContext(mockAmazonSQS, new QueueRequest(), null));

        verify(mockAmazonSQS, times(1)).receiveMessage(any(ReceiveMessageRequest.class));
        verify(mockAmazonSQS, times(1)).deleteMessage(any(DeleteMessageRequest.class));
        Assert.assertTrue(result.isSuccessful());
        Assert.assertEquals(result.getData().size(), 1);
        Assert.assertTrue(result.getData().contains(EXPECTED_BODY));
    }
}
