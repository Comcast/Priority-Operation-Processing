package com.theplatform.dfh.endpoint.api;

import java.io.Serializable;

public class DefaultServiceResponse implements ServiceResponse, Serializable
{
    public static final long serialVersionUID = 2007846549345987828L;

    private RuntimeServiceException exception;

    public RuntimeServiceException getException()
    {
        return exception;
    }

    public void setException(RuntimeServiceException exception)
    {
        this.exception = exception;
    }

    public boolean isError()
    {
        return exception != null;
    }

}
