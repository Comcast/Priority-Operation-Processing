package com.comcast.pop.modules.kube.fabric8.client.exception;

public class PodException extends RuntimeException
{
    public PodException()
    {
    }

    public PodException(String message)
    {
        super(message);
    }

    public PodException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PodException(Throwable cause)
    {
        super(cause);
    }

    public PodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}