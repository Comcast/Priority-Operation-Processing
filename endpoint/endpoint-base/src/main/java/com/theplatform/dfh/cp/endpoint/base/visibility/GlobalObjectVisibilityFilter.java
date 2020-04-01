package com.theplatform.dfh.cp.endpoint.base.visibility;

import com.theplatform.dfh.cp.api.GlobalEndpointDataObject;
import com.comcast.fission.endpoint.api.ServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalObjectVisibilityFilter <T extends GlobalEndpointDataObject, Req extends ServiceRequest> extends VisibilityFilter<T, Req>
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean isVisible(Req req, T object)
    {
        if(req == null || object == null)
        {
            if(logger.isDebugEnabled()) logger.debug("visibility = false. No request or data object");
            return false;
        }
        if(logger.isDebugEnabled()) logger.debug("visibility check : object.isGlobal = {}", object.isGlobal());

        return object.isGlobal();
    }

}
