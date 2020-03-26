package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

public class AnyMatchVisibilityFilter<T extends IdentifiedObject, Req extends ServiceRequest> extends VisibilityFilter<T, Req>
{
    private List<VisibilityFilter> filters;

    public AnyMatchVisibilityFilter(List<VisibilityFilter> filters)
    {
        this.filters = filters;
    }
    public AnyMatchVisibilityFilter()
    {
        this.filters = new ArrayList<>();
    }
    public AnyMatchVisibilityFilter withFilter(VisibilityFilter filter)
    {
        this.filters.add(filter);
        return this;
    }
    @Override
    public boolean isVisible(Req req, T object)
    {
        if(filters == null) return false;

        return filters.stream().anyMatch(f -> f.isVisible(req, object));
    }
}