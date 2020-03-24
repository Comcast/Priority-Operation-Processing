package com.theplatform.dfh.modules.queue.api;

import java.util.Collection;

/**
 * Result object when making queue requests
 */
public class QueueResult<T>
{
    private boolean successful;
    private Collection<T> data;
    private String message;

    public QueueResult()
    {
    }

    public QueueResult(boolean successful, Collection<T> data, String message)
    {
        this.successful = successful;
        this.data = data;
        this.message = message;
    }

    public boolean isSuccessful()
    {
        return successful;
    }

    public QueueResult<T> setSuccessful(boolean successful)
    {
        this.successful = successful;
        return this;
    }

    public Collection<T> getData()
    {
        return data;
    }

    public QueueResult<T> setData(Collection<T> data)
    {
        this.data = data;
        return this;
    }

    public String getMessage()
    {
        return message;
    }

    public QueueResult<T> setMessage(String message)
    {
        this.message = message;
        return this;
    }
}
