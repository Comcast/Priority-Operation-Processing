package com.theplatform.dfh.endpoint.api;

import java.io.IOException;

public class ValidationException extends IOException
{
    private static final int statusCode = 422;

    public ValidationException(String message)
    {
        super("status[" +statusCode +"]" +message);
    }

    public ValidationException(String message, Throwable cause)
    {
        super("status[" +statusCode +"]" +message, cause);
    }

    public ValidationException(Throwable cause)
    {
        super("status[" +statusCode +"]", cause);
    }
}
