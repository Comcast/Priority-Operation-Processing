package com.comcast.pop.modules.queue.aws.sqs.processor;

import com.comcast.pop.modules.queue.aws.sqs.SQSRequestContext;
import com.comcast.pop.modules.queue.aws.sqs.api.QueueRequest;
import com.comcast.pop.modules.queue.aws.sqs.api.QueueRequestType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic request processor that will call the type-specific request processor
 */
public class TypeRequestProcessor implements SQSRequestProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(TypeRequestProcessor.class);

    private Map<String, SQSRequestProcessor> requestProcessorMap;

    public TypeRequestProcessor()
    {
        requestProcessorMap = new HashMap<>();
        // TODO: create on demand! (no need to waste cycles creating unused processors)
        requestProcessorMap.put(QueueRequestType.add.name(), new AddRequestProcessor());
        requestProcessorMap.put(QueueRequestType.poll.name(), new PollRequestProcessor());
        requestProcessorMap.put(QueueRequestType.size.name(), new SizeRequestProcessor());
    }

    @Override
    public SQSQueueResult processRequest(SQSRequestContext sqsRequestContext)
    {
        SQSQueueResult requestResult;
        QueueRequest queueRequest = sqsRequestContext.getQueueRequest();

        logger.info("Attempting to process requestType: {}", queueRequest.getRequestType());

        SQSRequestProcessor processor = requestProcessorMap.get(queueRequest.getRequestType());
        if(processor == null)
        {
            // todo: log an error and exit
            requestResult = new SQSQueueResult()
                .setSuccessful(false)
                .setMessage(String.format("No processor found for request type: %1$s", queueRequest.getRequestType()));
        }
        else
        {
            try
            {
                requestResult = processor.processRequest(sqsRequestContext);
            }
            catch (Exception e)
            {
                // TODO: expand this to be more detailed
                logger.error(String.format("Failed to process SQS Action: %1$s", queueRequest.getRequestType()), e);
                requestResult = new SQSQueueResult();
                requestResult.setSuccessful(false);
            }
        }
        return requestResult;
    }

    // only needed for unit tests
    protected Map<String, SQSRequestProcessor> getRequestProcessorMap()
    {
        return requestProcessorMap;
    }

    public void setRequestProcessorMap(
            Map<String, SQSRequestProcessor> requestProcessorMap)
    {
        this.requestProcessorMap = requestProcessorMap;
    }
}
