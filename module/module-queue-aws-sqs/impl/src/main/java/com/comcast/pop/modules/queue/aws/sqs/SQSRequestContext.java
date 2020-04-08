package com.comcast.pop.modules.queue.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.comcast.pop.modules.queue.aws.sqs.api.QueueRequest;

public class SQSRequestContext
{
    private AmazonSQS sqsClient;
    private QueueRequest queueRequest;
    private String queueURL;

    public SQSRequestContext(AmazonSQS sqsClient, QueueRequest queueRequest,
            String queueURL)
    {
        this.sqsClient = sqsClient;
        this.queueRequest = queueRequest;
        this.queueURL = queueURL;
    }

    public AmazonSQS getSqsClient()
    {
        return sqsClient;
    }

    public QueueRequest getQueueRequest()
    {
        return queueRequest;
    }

    public String getQueueURL()
    {
        return queueURL;
    }
}
