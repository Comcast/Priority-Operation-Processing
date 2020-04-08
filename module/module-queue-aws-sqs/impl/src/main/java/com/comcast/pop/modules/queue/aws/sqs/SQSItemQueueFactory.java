package com.comcast.pop.modules.queue.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.comcast.pop.modules.queue.api.ItemQueue;
import com.comcast.pop.modules.queue.api.ItemQueueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SQSItemQueueFactory<T> implements ItemQueueFactory<T>
{
    private static final Logger logger = LoggerFactory.getLogger(SQSItemQueueFactory.class);

    private final AmazonSQS amazonSQS;
    private AWSQueueLookup awsQueueLookup = new AWSQueueLookup();
    private final Class clazz;

    public SQSItemQueueFactory(AmazonSQS amazonSQS, Class clazz)
    {
        this.amazonSQS = amazonSQS;
        this.clazz = clazz;
    }

    @Override
    public ItemQueue<T> createItemQueue(String queueName)
    {
        String queueURL = awsQueueLookup.getQueueUrl(amazonSQS, queueName);
        if(queueURL == null)
        {
            logger.error("Failed to lookup queue URL by name: {}", queueName);
            return null;
        }
        return new SQSItemQueue<>(amazonSQS, queueURL, clazz);
    }

    public void setAwsQueueLookup(AWSQueueLookup awsQueueLookup)
    {
        this.awsQueueLookup = awsQueueLookup;
    }
}
