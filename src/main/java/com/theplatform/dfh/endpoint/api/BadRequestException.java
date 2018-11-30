package com.theplatform.dfh.endpoint.api;

import java.io.IOException;

public class BadRequestException extends IOException
{
    private static final int statusCode = 400;

    public BadRequestException(String message)
    {
        super("status[" +statusCode +"]" +message);
    }

    public BadRequestException(String message, Throwable cause)
    {
        super("status[" +statusCode +"]" +message, cause);
    }

    public BadRequestException(Throwable cause)
    {
        super("status[" +statusCode +"]", cause);
    }
}
