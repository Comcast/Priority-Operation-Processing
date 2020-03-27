package com.theplatform.module.docker.exception;

public class FissionDockerException extends Exception
{
    public FissionDockerException()
    {
        super();
    }

    public FissionDockerException(String message)
    {
        super(message);
    }

    public FissionDockerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public FissionDockerException(Throwable cause)
    {
        super(cause);
    }
}
