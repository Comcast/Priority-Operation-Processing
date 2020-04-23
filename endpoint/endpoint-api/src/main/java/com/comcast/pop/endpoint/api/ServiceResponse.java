package com.comcast.pop.endpoint.api;

public interface ServiceResponse<T>
{
    T getErrorResponse();

    void setErrorResponse(T errorResponse);

    boolean isError();

    String getCID();

    void setCID(String cid);
}
