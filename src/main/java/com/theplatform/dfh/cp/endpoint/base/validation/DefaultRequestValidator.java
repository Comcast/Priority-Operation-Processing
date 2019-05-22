package com.theplatform.dfh.cp.endpoint.base.validation;

import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;

/**
 * Basic request validation
 * @param <R> The type of request
 */
public class DefaultRequestValidator<R extends ServiceRequest> implements RequestValidator<R>
{
    @Override
    public void validateGET(R request)
    {
        validateRequest(request);
    }

    @Override
    public void validatePOST(R request)
    {
        validateRequest(request);
    }

    @Override
    public void validatePUT(R request)
    {
        validateRequest(request);
    }

    @Override
    public void validateDELETE(R request)
    {
        validateRequest(request);
    }

    protected void validateRequest(R request)
    {
        if(request == null)
            throw new ValidationException("The request cannot be null.");
    }
}
