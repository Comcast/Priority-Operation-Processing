package com.theplatform.dfh.cp.handler.sample.impl.exception;

public class DfhSampleException extends RuntimeException
{
    public DfhSampleException(String message)
    {
        super(message);
    }

    public DfhSampleException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DfhSampleException(Throwable cause)
    {
        super(cause);
    }
}
