package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;

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
