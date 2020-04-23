package com.comcast.pop.handler.sample.impl.exception;

public class SampleHandlerException extends RuntimeException
{
    public SampleHandlerException(String message)
    {
        super(message);
    }

    public SampleHandlerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SampleHandlerException(Throwable cause)
    {
        super(cause);
    }
}
