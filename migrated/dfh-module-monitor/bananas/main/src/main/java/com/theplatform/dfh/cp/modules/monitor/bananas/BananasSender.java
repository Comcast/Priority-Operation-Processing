package com.theplatform.dfh.cp.modules.monitor.bananas;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.modules.monitor.RetriableRequester;
import com.theplatform.dfh.cp.modules.monitor.alert.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class BananasSender extends RetriableRequester implements AlertSender
{
    private static final Logger logger = LoggerFactory.getLogger(BananasSender.class);
    private static final int REQUEST_TIMEOUT = 10000;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String host;
    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public BananasSender(String host, Integer retryTimeout, Integer retryCount, Set<Class<? extends Throwable>> retriableExceptions)
    {
        super(retryTimeout, retryCount, retriableExceptions);
        if(host == null)
            throw new IllegalArgumentException("A host must be defined for Bananas alerting.");
        this.host = host;
    }
    public BananasSender(String host, Integer retryTimeout, Integer retryCount)
    {
        this(host, retryTimeout, retryCount, null);
    }

    public BananasSender(String host)
    {
        this(host, null, null, null);
    }

    /**
     *  Attempts to send the message to the bananas endpoint 5 times before giving up.
     * @param bananasMessage the message to send to the bananas endpoint
     */
    public void send(AlertMessage bananasMessage)
    {
        try
        {
            final String messagePayload = objectMapper.writeValueAsString(bananasMessage);
            retry(() -> sendMessage(bananasMessage.getLevel(), messagePayload));
        }
        catch (Throwable e)
        {
            logger.error("An exception occurred trying to send alert to Banana at {}", host, e);
            throw new AlertException(e);
        }
    }

    protected Boolean sendMessage(AlertLevel level, String messagePayload) throws Exception
    {
        logger.info("Attempting to send {} alert to Banana at {}", level, host);
        if (logger.isDebugEnabled())
            logger.debug("Sending the following payload Banana alert endpoint {}. {}",host, messagePayload);

        HttpPost httpPost = new HttpPost(host);
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(REQUEST_TIMEOUT)
            .setSocketTimeout(REQUEST_TIMEOUT)
            .build();
        httpPost.setConfig(requestConfig);

        StringEntity entity = new StringEntity(messagePayload);
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        //make sure both the client and the http response get auto-closed; any exceptions are caught and logged
        CloseableHttpClient client = HttpClients.createDefault();
        CloseableHttpResponse response = client.execute(httpPost);
        final int responseStatusCode = response.getStatusLine().getStatusCode();
        if (responseStatusCode == 200)
        {
            logger.info("Successful post to Bananas made.");
        }
        else
        {
            logger.error("Unsuccessful post to Bananas {}. Response code {}", host, responseStatusCode);
        }
        return true;
    }
}
