package com.theplatform.dfh.modules.queue.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;

public interface AmazonSQSClientFactory
{
    AmazonSQS createClient();
}
