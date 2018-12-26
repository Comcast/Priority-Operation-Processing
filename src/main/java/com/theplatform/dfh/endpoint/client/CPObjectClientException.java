package com.theplatform.dfh.endpoint.client;

public class CPObjectClientException extends RuntimeException
{
    public CPObjectClientException(String message)
    {
        super(message);
    }

    public CPObjectClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CPObjectClientException(Throwable cause)
    {
        super(cause);
    }
}
