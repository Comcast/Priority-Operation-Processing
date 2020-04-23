package com.comcast.pop.endpoint.api.data;

import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.object.api.IdentifiedObject;
import com.comcast.pop.persistence.api.query.Query;

import java.util.List;

public interface DataObjectRequest<T extends IdentifiedObject> extends ServiceRequest<T>
{
    String getId();
    T getDataObject();
    List<Query> getQueries();
    void setQueries(List<Query> queries);
}
