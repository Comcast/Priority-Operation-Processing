package com.theplatform.dfh.modules.queue.aws.sqs.api;

import java.util.Collection;
import java.util.Map;

/**
 * Request definition for making queue requests
 */
public class QueueRequest
{
    /**
     * Maps to a QueueRequestType
     */
    private String requestType;
    /**
     * The queue name to operate with
     */
    private String queueName;
    /**
     * The (optional) data objects to pass along with the request
     */
    private Collection<String> data;
    /**
     * The (optional) arguments to pass along with the request
     */
    private Map<String, String> arguments;

    public QueueRequest(){}

    public QueueRequest(String requestType, String queueName, Collection<String> data, Map<String, String> arguments)
    {
        this.requestType = requestType;
        this.queueName = queueName;
        this.data = data;
        this.arguments = arguments;
    }

    public String getRequestType()
    {
        return requestType;
    }

    public QueueRequest setRequestType(String requestType)
    {
        this.requestType = requestType;
        return this;
    }

    public String getQueueName()
    {
        return queueName;
    }

    public QueueRequest setQueueName(String queueName)
    {
        this.queueName = queueName;
        return this;
    }

    public Collection<String> getData()
    {
        return data;
    }

    public QueueRequest setData(Collection<String> data)
    {
        this.data = data;
        return this;
    }

    public Map<String, String> getArguments()
    {
        return arguments;
    }

    public QueueRequest setArguments(Map<String, String> arguments)
    {
        this.arguments = arguments;
        return this;
    }
}
