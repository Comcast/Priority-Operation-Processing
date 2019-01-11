package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.List;

public interface VisibilityFilter<T extends IdentifiedObject, Req extends ServiceRequest>
{
    boolean isVisible(Req req, T object);
    List<T> filterByVisible(Req req, List<T> objects);
}
