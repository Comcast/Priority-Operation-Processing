package com.comcast.fission.endpoint.api.data;

import com.comcast.fission.endpoint.api.ServiceRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

public interface DataObjectRequest<T extends IdentifiedObject> extends ServiceRequest<T>
{
    String getId();
    T getDataObject();
    List<Query> getQueries();
    void setQueries(List<Query> queries);
}