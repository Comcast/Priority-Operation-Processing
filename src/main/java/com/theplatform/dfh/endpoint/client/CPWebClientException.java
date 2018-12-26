package com.theplatform.dfh.endpoint.client;

public class CPWebClientException extends RuntimeException
{
    public CPWebClientException(String message)
    {
        super(message);
    }

    public CPWebClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CPWebClientException(Throwable cause)
    {
        super(cause);
    }
}