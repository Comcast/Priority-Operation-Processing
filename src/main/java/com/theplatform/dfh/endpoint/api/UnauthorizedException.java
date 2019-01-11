package com.theplatform.dfh.endpoint.api;

public class UnauthorizedException extends RuntimeServiceException
{
    private static final int statusCode = 402;

    public UnauthorizedException()
    {
        super(statusCode);
    }

    public UnauthorizedException(String message)
    {
        super(message, statusCode);
    }

    public UnauthorizedException(String message, Throwable cause)
    {
        super(message, cause, statusCode);
    }

    public UnauthorizedException(Throwable cause)
    {
        super(cause, statusCode);
    }
}
