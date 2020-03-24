package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSRequestContext;

/**
 * Requests the size of the SQS
 */
public class SizeRequestProcessor implements SQSRequestProcessor
{
    @Override
    public SQSQueueResult processRequest(SQSRequestContext sqsRequestContext)
    {
        SQSQueueResult result = new SQSQueueResult();

        GetQueueAttributesRequest queueAttributesRequest = new GetQueueAttributesRequest()
                .withQueueUrl(sqsRequestContext.getQueueURL())
                .withAttributeNames(
                        QueueAttributeName.ApproximateNumberOfMessages.name()
                );
        GetQueueAttributesResult queueAttributesResult =
                sqsRequestContext.getSqsClient().getQueueAttributes(queueAttributesRequest);

        // TODO: investigate any possible validation of the result
        String messageCount =
                queueAttributesResult.getAttributes().get(QueueAttributeName.ApproximateNumberOfMessages.name());

        result.setSuccessful(messageCount != null);
        result.setMessage(messageCount);
        return result;
    }
}
