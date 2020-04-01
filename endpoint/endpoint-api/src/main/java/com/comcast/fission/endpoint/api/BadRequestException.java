package com.comcast.fission.endpoint.api;


public class BadRequestException extends RuntimeServiceException
{
    private static final int statusCode = 400;

    public BadRequestException()
    {
        super(statusCode);
    }

    public BadRequestException(String message)
    {
        super(message, statusCode);
    }

    public BadRequestException(String message, Throwable cause)
    {
        super(message, cause, statusCode);
    }

    public BadRequestException(Throwable cause)
    {
        super(cause, statusCode);
    }
}
