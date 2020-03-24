package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class VisibilityFilter<T extends IdentifiedObject, Req extends ServiceRequest>
{
    public abstract boolean isVisible(Req req, T object);

    public List<T> filterByVisible(Req req, List<T> objects)
    {
        if(objects == null) return new ArrayList<>();

        return objects.stream().filter(o -> isVisible(req, o)).collect(Collectors.toList());
    }
}
