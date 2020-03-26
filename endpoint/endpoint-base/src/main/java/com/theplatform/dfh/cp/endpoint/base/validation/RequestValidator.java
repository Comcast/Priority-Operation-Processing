package com.theplatform.dfh.cp.endpoint.base.validation;

import com.theplatform.dfh.endpoint.api.ServiceRequest;

public interface RequestValidator<R extends ServiceRequest>
{
    void validateGET(R request);
    void validatePOST(R request);
    void validatePUT(R request);
    void validateDELETE(R request);
}
