package com.theplatform.dfh.cp.handler.puller.impl.exception;

/**
 *
 */
public class PullerException extends RuntimeException
{
    public PullerException(Exception e)
    {
        super(e);
    }

    public PullerException(String message)
    {
        super(message);
    }

    public PullerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}