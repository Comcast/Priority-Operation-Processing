package com.theplatform.module.docker.exception;

public class DfhDockerException extends Exception
{
    public DfhDockerException()
    {
        super();
    }

    public DfhDockerException(String message)
    {
        super(message);
    }

    public DfhDockerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DfhDockerException(Throwable cause)
    {
        super(cause);
    }
}
