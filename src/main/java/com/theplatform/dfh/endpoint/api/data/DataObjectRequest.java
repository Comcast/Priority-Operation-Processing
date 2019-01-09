package com.theplatform.dfh.endpoint.api.data;

import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.query.Query;

import java.util.List;

public interface DataObjectRequest<T extends IdentifiedObject> extends ServiceRequest<T>
{
    String getId();
    T getDataObject();
    List<Query> getQueries();
}
