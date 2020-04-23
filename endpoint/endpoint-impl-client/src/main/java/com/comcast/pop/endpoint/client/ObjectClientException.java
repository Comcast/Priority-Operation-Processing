package com.comcast.pop.endpoint.client;

public class ObjectClientException extends RuntimeException
{
    public ObjectClientException(String message)
    {
        super(message);
    }

    public ObjectClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ObjectClientException(Throwable cause)
    {
        super(cause);
    }
}
