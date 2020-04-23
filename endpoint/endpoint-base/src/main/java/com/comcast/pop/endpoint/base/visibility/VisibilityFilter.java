package com.comcast.pop.endpoint.base.visibility;

import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.object.api.IdentifiedObject;

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
