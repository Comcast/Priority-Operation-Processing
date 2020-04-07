package com.theplatform.dfh.cp.endpoint.validation;

import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.object.api.IdentifiedObject;

public abstract class BaseValidatorTest<T extends IdentifiedObject>
{
    protected DataObjectRequest<T> createRequest(T agenda)
    {
        DefaultDataObjectRequest<T> objectRequest = new DefaultDataObjectRequest<>();
        objectRequest.setDataObject(agenda);
        return objectRequest;
    }
}
