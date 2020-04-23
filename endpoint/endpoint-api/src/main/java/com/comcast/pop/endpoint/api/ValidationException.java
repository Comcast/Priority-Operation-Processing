package com.comcast.pop.endpoint.api;

public class ValidationException extends RuntimeServiceException
{
    private static final int statusCode = 422;

    public ValidationException()
    {
        super(statusCode);
    }

    public ValidationException(String message)
    {
        super(message, statusCode);
    }

    public ValidationException(String message, Throwable cause)
    {
        super(message, cause, statusCode);
    }

    public ValidationException(Throwable cause)
    {
        super(cause, statusCode);
    }
}
