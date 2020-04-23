package com.comcast.pop.modules.queue.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;

public class AmazonSQSClientFactoryImpl implements AmazonSQSClientFactory
{
    public AmazonSQS createClient()
    {
        return AmazonSQSAsyncClientBuilder.defaultClient();
    }
}
