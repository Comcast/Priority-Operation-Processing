package com.comcast.pop.endpoint.client;

public class POPClientException extends RuntimeException
{
    public POPClientException(String message)
    {
        super(message);
    }

    public POPClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public POPClientException(Throwable cause)
    {
        super(cause);
    }
}