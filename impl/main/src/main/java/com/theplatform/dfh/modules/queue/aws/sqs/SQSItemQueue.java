package com.theplatform.dfh.modules.queue.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.modules.queue.aws.sqs.api.QueueRequest;
import com.theplatform.dfh.modules.queue.aws.sqs.api.QueueRequestArgument;
import com.theplatform.dfh.modules.queue.aws.sqs.processor.AddRequestProcessor;
import com.theplatform.dfh.modules.queue.aws.sqs.processor.PollRequestProcessor;
import com.theplatform.dfh.modules.queue.aws.sqs.processor.SQSQueueResult;
import com.theplatform.dfh.modules.queue.aws.sqs.processor.SizeRequestProcessor;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SQS Item Queue implementation for direct usage in AWS executing code (lambda generally)
 * @param <T> The type of object stored in the queue
 */
public class SQSItemQueue<T> implements ItemQueue<T>
{
    private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Class<T> clazz;
    private AmazonSQS amazonSQS;
    private String queueName;
    private AWSQueueLookup awsQueueLookup = new AWSQueueLookup();
    private AddRequestProcessor addRequestProcessor = new AddRequestProcessor();
    private PollRequestProcessor pollRequestProcessor = new PollRequestProcessor();
    private SizeRequestProcessor sizeRequestProcessor = new SizeRequestProcessor();

    public SQSItemQueue(AmazonSQS amazonSQS, String queueName, Class clazz)
    {
        this.amazonSQS = amazonSQS;
        this.queueName = queueName;
        this.clazz = clazz;
    }

    @Override
    public QueueResult<T> add(T item)
    {
        return add(Collections.singletonList(item));
    }

    @Override
    public QueueResult<T> add(Collection<T> items)
    {
        try
        {
            List<String> dataStrings = items.stream().map(item ->
            {
                try
                {
                    if(clazz == String.class) return (String)item;
                    return objectMapper.writeValueAsString(item);
                }
                catch (JsonProcessingException e)
                {
                    throw new RuntimeException(e);
                }
            }
            ).collect(Collectors.toList());

            SQSQueueResult sqsQueueResult = addRequestProcessor.processRequest(createRequestContext(new QueueRequest().setData(dataStrings)));
            return new QueueResult<T>().setSuccessful(sqsQueueResult.isSuccessful()).setMessage(sqsQueueResult.getMessage());
        }
        catch(RuntimeException e)
        {
            return new QueueResult<T>().setSuccessful(false).setMessage(e.getMessage());
        }
    }

    @Override
    public QueueResult<T> peek()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public QueueResult<T> poll()
    {
        return poll(1);
    }

    @Override
    public QueueResult<T> poll(int maxPollCount)
    {
        Map<String, String> argMap = new HashMap<>();
        argMap.put(QueueRequestArgument.maxResults.name(), Integer.toString(maxPollCount));
        SQSQueueResult sqsQueueResult = pollRequestProcessor.processRequest(createRequestContext(new QueueRequest().setArguments(argMap)));
        if(clazz == String.class)
        {
            // just return it
            return (QueueResult<T>)sqsQueueResult;
        }
        else if(sqsQueueResult.isSuccessful())
        {
            List<T> pojos = new LinkedList<>();
            QueueResult<T> queueResult = new QueueResult<>(true, null, null);
            for (String json : sqsQueueResult.getData())
            {
                try
                {
                    pojos.add(objectMapper.readValue(json, clazz));
                }
                catch(IOException e)
                {
                    queueResult.setSuccessful(false);
                    queueResult.setMessage(e.getMessage());
                    break;
                }
            }
            if(queueResult.isSuccessful()) queueResult.setData(pojos);
            return queueResult;
        }
        else
        {
            return new QueueResult<>(false, null, sqsQueueResult.getMessage());
        }
    }

    @Override
    public QueueResult<T> size()
    {
        SQSQueueResult sqsQueueResult = sizeRequestProcessor.processRequest(createRequestContext(new QueueRequest()));
        return new QueueResult<>(sqsQueueResult.isSuccessful(), null, sqsQueueResult.getMessage());
    }

    protected SQSRequestContext createRequestContext(QueueRequest queueRequest)
    {
        return new SQSRequestContext(
            amazonSQS,
            queueRequest,
            awsQueueLookup.getQueueUrl(amazonSQS, queueName));
    }

    public ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public AddRequestProcessor getAddRequestProcessor()
    {
        return addRequestProcessor;
    }

    public void setAddRequestProcessor(AddRequestProcessor addRequestProcessor)
    {
        this.addRequestProcessor = addRequestProcessor;
    }

    public PollRequestProcessor getPollRequestProcessor()
    {
        return pollRequestProcessor;
    }

    public void setPollRequestProcessor(PollRequestProcessor pollRequestProcessor)
    {
        this.pollRequestProcessor = pollRequestProcessor;
    }

    public SizeRequestProcessor getSizeRequestProcessor()
    {
        return sizeRequestProcessor;
    }

    public void setSizeRequestProcessor(SizeRequestProcessor sizeRequestProcessor)
    {
        this.sizeRequestProcessor = sizeRequestProcessor;
    }

    public AWSQueueLookup getAwsQueueLookup()
    {
        return awsQueueLookup;
    }

    public void setAwsQueueLookup(AWSQueueLookup awsQueueLookup)
    {
        this.awsQueueLookup = awsQueueLookup;
    }
}
