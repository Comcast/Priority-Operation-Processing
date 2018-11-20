package com.theplatform.dfh.modules.queue.aws.sqs.processor;

import com.amazonaws.services.sqs.model.*;
import com.theplatform.dfh.modules.queue.aws.sqs.SQSRequestContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Adds an item to the SQS
 */
public class AddRequestProcessor implements SQSRequestProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(AddRequestProcessor.class);

    private MessageDigestUtil messageDigestUtil;
    private UUIDGenerator uuidGenerator;

    public AddRequestProcessor()
    {
        messageDigestUtil = new MessageDigestUtil();
        uuidGenerator = new UUIDGenerator();
    }

    @Override
    public SQSQueueResult processRequest(SQSRequestContext sqsRequestContext)
    {
        Map<String, String> idMD5Map = new HashMap<>();

        Collection<String> data = sqsRequestContext.getQueueRequest().getData();
        // nothing to add is a success
        if(data == null) return new SQSQueueResult().setSuccessful(true);

        SendMessageBatchRequest sendMessageBatchRequest;

        try
        {
            sendMessageBatchRequest = new SendMessageBatchRequest()
                .withQueueUrl(sqsRequestContext.getQueueURL())
                .withEntries(data.stream().map(item ->
                        createSendMessageBatchRequestEntry(idMD5Map, item)
                    ).collect(Collectors.toList())
                );
        }
        catch(RuntimeException e)
        {
            return new SQSQueueResult()
                .setSuccessful(false)
                .setMessage(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
        }

        SendMessageBatchResult sendMessageBatchResult = sqsRequestContext.getSqsClient().sendMessageBatch(sendMessageBatchRequest);

        if(sendMessageBatchResult.getFailed() != null && sendMessageBatchResult.getFailed().size() > 0)
        {
            return new SQSQueueResult()
                .setSuccessful(false)
                .setMessage(String.format("Failure to persist [%1$s] messages", sendMessageBatchResult.getFailed().size()));
        }

        AtomicBoolean md5Errors = new AtomicBoolean(false);
        sendMessageBatchResult.getSuccessful().forEach(
            result ->
            {
                if(!validateResponse(idMD5Map.get(result.getId()), result.getMD5OfMessageBody()))
                {
                    logger.error(String.format("MD5 of source body does not match result body: [%1$s][%2$s]", idMD5Map.get(result.getId()), result.getMD5OfMessageBody()));
                    md5Errors.set(true);
                }
            }
        );

        if(md5Errors.get())
        {
            return new SQSQueueResult()
                .setSuccessful(false)
                .setMessage("MD5 mismatches when persisting to queue");
        }
        return new SQSQueueResult()
            .setSuccessful(true);
    }

    private SendMessageBatchRequestEntry createSendMessageBatchRequestEntry(Map<String, String> idMD5Map, String messageBody)
    {
        String id = uuidGenerator.generate();
        String md5String;
        try
        {
            md5String = messageDigestUtil.getMD5String(messageBody);
        }
        catch(NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        idMD5Map.put(id, md5String);
        return new SendMessageBatchRequestEntry()
            .withId(id)
            .withMessageBody(messageBody);
    }

    /**
     * Validates the sendMessage response by checking the source and resulting md5
     * @param md5Source The md5 string for the source data
     * @param md5result The md5 string for the resulting data
     * @return true if the response matches the source, false otherwise
     */
    private boolean validateResponse(String md5Source, String md5result)
    {
        if(md5Source == null || md5result == null) return false;
        String trimmedSource = md5Source.trim();
        String trimmedResult = md5result.trim();
        return StringUtils.equalsIgnoreCase(trimmedSource, trimmedResult);
    }

    public void setMessageDigestUtil(MessageDigestUtil messageDigestUtil)
    {
        this.messageDigestUtil = messageDigestUtil;
    }

    public void setUuidGenerator(UUIDGenerator uuidGenerator)
    {
        this.uuidGenerator = uuidGenerator;
    }
}
