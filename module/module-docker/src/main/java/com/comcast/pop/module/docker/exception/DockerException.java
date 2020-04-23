package com.comcast.pop.module.docker.exception;

public class DockerException extends Exception
{
    public DockerException()
    {
        super();
    }

    public DockerException(String message)
    {
        super(message);
    }

    public DockerException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public DockerException(Throwable cause)
    {
        super(cause);
    }
}
