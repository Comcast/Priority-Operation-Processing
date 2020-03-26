package com.theplatform.dfh.cp.handler.sample.impl.exception;

public class FissionSampleHandlerException extends RuntimeException
{
    public FissionSampleHandlerException(String message)
    {
        super(message);
    }

    public FissionSampleHandlerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public FissionSampleHandlerException(Throwable cause)
    {
        super(cause);
    }
}
