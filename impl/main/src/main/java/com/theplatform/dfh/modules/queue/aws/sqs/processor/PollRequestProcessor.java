package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.theplatform.dfh.modules.queue.aws.sqs.api.QueueRequest;
import com.theplatform.dfh.modules.queue.aws.sqs.api.QueueRequestArgument;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Polls the SQS for a single item (removes it from the queue as well once taken)
 */
public class PollRequestProcessor implements SQSRequestProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(PollRequestProcessor.class);

    private int waitTimeSeconds = 5;

    @Override
    public SQSQueueResult processRequest(SQSRequestContext sqsRequestContext)
    {
        AmazonSQS amazonSQS = sqsRequestContext.getSqsClient();

        SQSQueueResult result = new SQSQueueResult();
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest()
                .withQueueUrl(sqsRequestContext.getQueueURL())
                .withMaxNumberOfMessages(getMaxMessages(sqsRequestContext));
        receiveMessageRequest.setWaitTimeSeconds(waitTimeSeconds);
        ReceiveMessageResult receiveMessageResult = amazonSQS.receiveMessage(receiveMessageRequest);
        if (receiveMessageResult.getMessages().size() >= 1)
        {
            List<String> messageBodies = new LinkedList<>();
            for (Message receivedMessage : receiveMessageResult.getMessages())
            {
                try
                {
                    DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                        .withQueueUrl(sqsRequestContext.getQueueURL())
                        .withReceiptHandle(receivedMessage.getReceiptHandle());
                    // NOTE/TODO: the response object from this request is useless
                    amazonSQS.deleteMessage(deleteMessageRequest);
                    messageBodies.add(receivedMessage.getBody());
                }
                catch(Exception e)
                {
                    // continue on, item not removed from the queue nor added to the results
                }
            }
            result
                .setSuccessful(true)
                .setData(messageBodies);
        }
        else
        {
            // no result from sqs is valid due to timing
            result.setSuccessful(true);
            result.setData(null);
        }
        return result;
    }

    private int getMaxMessages(SQSRequestContext sqsRequestContext)
    {
        QueueRequest queueRequest = sqsRequestContext.getQueueRequest();
        Map<String, String> argsMap = queueRequest.getArguments();
        if(argsMap != null && argsMap.containsKey(QueueRequestArgument.maxResults.name()))
        {
            try
            {
                return Integer.parseInt(argsMap.get(QueueRequestArgument.maxResults.name()));
            }
            catch(NumberFormatException e)
            {
                logger.warn(
                    String.format("Defaulting invalid maxResults [%1$s]", argsMap.get(QueueRequestArgument.maxResults.name())),
                    e);
            }
        }
        return 1;
    }

    public void setWaitTimeSeconds(int waitTimeSeconds)
    {
        this.waitTimeSeconds = waitTimeSeconds;
    }
}
