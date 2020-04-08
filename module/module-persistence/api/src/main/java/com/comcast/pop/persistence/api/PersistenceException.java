package com.comcast.pop.persistence.api;

public class PersistenceException extends Throwable
{
    public PersistenceException(String message)
    {
        super(message);
    }

    public PersistenceException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
