package com.comcast.pop.endpoint.api.data;

import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ServiceResponse;
import com.comcast.pop.object.api.IdentifiedObject;

import java.util.List;

public interface DataObjectResponse<D extends IdentifiedObject> extends ServiceResponse<ErrorResponse>
{
    void add(D dataObject);
    void addAll(List<D> dataObjects);
    List<D> getAll();
    Integer getCount();
    D getFirst();
}
