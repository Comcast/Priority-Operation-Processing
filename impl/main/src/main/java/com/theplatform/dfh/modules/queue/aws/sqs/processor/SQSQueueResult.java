package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import com.theplatform.dfh.modules.queue.api.QueueResult;

import java.util.Collection;

/**
 * Result object when making queue requests
 */
public class SQSQueueResult extends QueueResult<String>
{
    public SQSQueueResult()
    {

    }

    @Override
    public SQSQueueResult setSuccessful(boolean successful)
    {
        super.setSuccessful(successful);
        return this;
    }

    @Override
    public SQSQueueResult setData(Collection<String> data)
    {
        super.setData(data);
        return this;
    }

    @Override
    public SQSQueueResult setMessage(String message)
    {
        super.setMessage(message);
        return this;
    }
}
