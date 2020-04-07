package com.comcast.pop.endpoint.base.visibility;

import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.object.api.IdentifiedObject;

import java.util.HashMap;

public class VisibilityFilterMap<T extends IdentifiedObject, Req extends ServiceRequest>
{
    private final CustomerVisibilityFilter<T,Req> defaultFilter = new CustomerVisibilityFilter<>();
    private HashMap<VisibilityMethod, VisibilityFilter<T,Req>> filterMap = new HashMap<>();

    public VisibilityFilterMap()
    {
        put(VisibilityMethod.GET, defaultFilter);
        put(VisibilityMethod.POST, defaultFilter);
        put(VisibilityMethod.PUT, defaultFilter);
        put(VisibilityMethod.DELETE, defaultFilter);
    }

    public void put(VisibilityMethod visibilityMethod, VisibilityFilter<T,Req> filter)
    {
        filterMap.put(visibilityMethod, filter);
    }

    public void putForRead(VisibilityFilter<T,Req> filter)
    {
        put(VisibilityMethod.GET, filter);
    }

    public void putForWrite(VisibilityFilter<T,Req> filter)
    {
        put(VisibilityMethod.POST, filter);
        put(VisibilityMethod.PUT, filter);
        put(VisibilityMethod.DELETE, filter);
    }

    public VisibilityFilter<T,Req> get(VisibilityMethod visibilityMethod)
    {
        VisibilityFilter filter = filterMap.get(visibilityMethod);
        return filter != null ? filter : defaultFilter;
    }
}
