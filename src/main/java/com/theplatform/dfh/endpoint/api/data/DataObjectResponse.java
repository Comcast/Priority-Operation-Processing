package com.theplatform.dfh.endpoint.api.data;

import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ServiceResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.List;

public interface DataObjectResponse<D extends IdentifiedObject> extends ServiceResponse<ErrorResponse>
{
    void add(D dataObject);
    void addAll(List<D> dataObjects);
    List<D> getAll();
    D getFirst();
}
