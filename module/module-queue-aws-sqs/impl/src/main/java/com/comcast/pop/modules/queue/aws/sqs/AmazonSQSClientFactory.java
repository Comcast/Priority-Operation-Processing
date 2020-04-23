package com.comcast.pop.modules.queue.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;

public interface AmazonSQSClientFactory
{
    AmazonSQS createClient();
}
