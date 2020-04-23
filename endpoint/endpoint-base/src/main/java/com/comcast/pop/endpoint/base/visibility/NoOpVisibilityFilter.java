package com.comcast.pop.endpoint.base.visibility;

import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.object.api.IdentifiedObject;

/**
 * Always visible filter
 * @param <T> Type of IdentifiedObject
 * @param <Req> Type of ServiceRequest
 */
public class NoOpVisibilityFilter<T extends IdentifiedObject, Req extends ServiceRequest> extends VisibilityFilter<T, Req>
{
    @Override
    public boolean isVisible(Req req, T object)
    {
        return true;
    }
}
