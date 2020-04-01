package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.comcast.fission.endpoint.api.ServiceRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

public class AllMatchVisibilityFilter<T extends IdentifiedObject, Req extends ServiceRequest> extends VisibilityFilter<T, Req>
{
    private List<VisibilityFilter> filters;

    public AllMatchVisibilityFilter(List<VisibilityFilter> filters)
    {
        this.filters = filters;
    }
    public AllMatchVisibilityFilter()
    {
        this.filters = new ArrayList<>();
    }
    public AllMatchVisibilityFilter withFilter(VisibilityFilter filter)
    {
        this.filters.add(filter);
        return this;
    }
    @Override
    public boolean isVisible(Req req, T object)
    {
        if(filters == null) return false;

        return filters.stream().allMatch(f -> f.isVisible(req, object));
    }
}
