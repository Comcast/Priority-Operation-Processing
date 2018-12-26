package com.theplatform.dfh.cp.endpoint.facility;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * Request processor for the Facility Endpoint
 */
public class ResourcePoolRequestProcessor extends BaseRequestProcessor<ResourcePool>
{
    public ResourcePoolRequestProcessor(ObjectPersister<ResourcePool> resourcePoolObjectPersister)
    {
        super(resourcePoolObjectPersister);
    }
}
