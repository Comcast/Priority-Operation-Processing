package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import com.theplatform.dfh.modules.queue.aws.sqs.SQSRequestContext;

public interface SQSRequestProcessor
{
    SQSQueueResult processRequest(SQSRequestContext sqsRequestContext);
}
