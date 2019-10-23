package com.theplatform.dfh.endpoint.api;

public interface ServiceResponse<T>
{
    T getErrorResponse();

    void setErrorResponse(T errorResponse);

    boolean isError();

    String getCID();
}
