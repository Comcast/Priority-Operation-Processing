package com.comcast.pop.modules.queue.aws.sqs;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AWSQueueLookup
{
    private static final Logger logger = LoggerFactory.getLogger(AWSQueueLookup.class);

    /**
     * Attempts to get the url of the specified queue name
     * @param sqs The AmazonSQS to operate with
     * @param queueName The name of the queue to look up
     * @return The queue url associated with the specified queue name
     */
    public String getQueueUrl(AmazonSQS sqs, String queueName)
    {
        // do not translate a url, not necessary
        if(StringUtils.startsWithIgnoreCase(queueName, "http")) return queueName;

        logger.info("Looking up queueUrl for: [{}]", queueName);
        GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(queueName);

        logger.info("Query success: Queue: [{}] URL: [{}]", queueName, getQueueUrlResult.getQueueUrl());
        return getQueueUrlResult.getQueueUrl();
    }
}
