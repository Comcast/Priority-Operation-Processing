package com.comcast.pop.modules.kube.fabric8.client.exception;

public class PodNotScheduledException extends PodException
{
    public PodNotScheduledException()
    {
    }

    public PodNotScheduledException(String message)
    {
        super(message);
    }

    public PodNotScheduledException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PodNotScheduledException(Throwable cause)
    {
        super(cause);
    }


}