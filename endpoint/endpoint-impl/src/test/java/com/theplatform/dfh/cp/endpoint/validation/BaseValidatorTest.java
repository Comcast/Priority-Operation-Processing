package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

public abstract class BaseValidatorTest<T extends IdentifiedObject>
{
    protected DataObjectRequest<T> createRequest(T agenda)
    {
        DefaultDataObjectRequest<T> objectRequest = new DefaultDataObjectRequest<>();
        objectRequest.setDataObject(agenda);
        return objectRequest;
    }
}
