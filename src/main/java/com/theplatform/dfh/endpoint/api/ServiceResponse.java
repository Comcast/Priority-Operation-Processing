package com.theplatform.dfh.endpoint.api;

public interface ServiceResponse
{
    RuntimeServiceException getException();

    void setException(RuntimeServiceException exception);

    boolean isError();
}
