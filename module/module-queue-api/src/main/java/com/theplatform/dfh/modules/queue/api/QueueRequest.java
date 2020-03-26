package com.theplatform.dfh.modules.queue.api;

import java.util.Collection;

/**
 * Request definition for making queue requests
 */
public class QueueRequest<T>
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
    private Collection<T> data;
    /**
     * The (optional) arguments to pass along with the request
     */
    private Collection<String> arguments;

    public QueueRequest(){}

    public QueueRequest(String requestType, String queueName, Collection<T> data, Collection<String> arguments)
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

    public QueueRequest<T> setRequestType(String requestType)
    {
        this.requestType = requestType;
        return this;
    }

    public String getQueueName()
    {
        return queueName;
    }

    public QueueRequest<T> setQueueName(String queueName)
    {
        this.queueName = queueName;
        return this;
    }

    public Collection<T> getData()
    {
        return data;
    }

    public QueueRequest<T> setData(Collection<T> data)
    {
        this.data = data;
        return this;
    }

    public Collection<String> getArguments()
    {
        return arguments;
    }

    public QueueRequest<T> setArguments(Collection<String> arguments)
    {
        this.arguments = arguments;
        return this;
    }
}
