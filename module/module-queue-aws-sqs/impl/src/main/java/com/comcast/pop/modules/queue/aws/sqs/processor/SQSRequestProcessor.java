package com.comcast.pop.modules.queue.aws.sqs.processor;

import com.comcast.pop.modules.queue.aws.sqs.SQSRequestContext;

public interface SQSRequestProcessor
{
    SQSQueueResult processRequest(SQSRequestContext sqsRequestContext);
}
