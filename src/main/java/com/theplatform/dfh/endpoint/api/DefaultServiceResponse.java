package com.theplatform.dfh.endpoint.api;

import java.io.Serializable;

public class DefaultServiceResponse implements ServiceResponse<ErrorResponse>, Serializable
{
    public static final long serialVersionUID = 2007846549345987828L;

    private String cid;

    private ErrorResponse errorResponse;

    public ErrorResponse getErrorResponse()
    {
        return errorResponse;
    }

    public void setErrorResponse(ErrorResponse errorResponse)
    {
        this.errorResponse = errorResponse;
    }

    public boolean isError()
    {
        return errorResponse != null;
    }

    @Override
    public String getCID()
    {
        return cid;
    }

    public void setCid(String cid)
    {
        this.cid = cid;
    }
}
