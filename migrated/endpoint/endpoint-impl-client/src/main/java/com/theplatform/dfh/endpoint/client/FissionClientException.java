package com.theplatform.dfh.endpoint.client;

public class FissionClientException extends RuntimeException
{
    public FissionClientException(String message)
    {
        super(message);
    }

    public FissionClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public FissionClientException(Throwable cause)
    {
        super(cause);
    }
}