package com.theplatform.dfh.cp.handler.executor.impl.exception;

/**
 *
 */
public class AgendaExecutorException extends RuntimeException
{
    public AgendaExecutorException(Exception e)
    {
        super(e);
    }

    public AgendaExecutorException(String message)
    {
        super(message);
    }

    public AgendaExecutorException(String message, Throwable cause)
    {
        super(message, cause);
    }
}