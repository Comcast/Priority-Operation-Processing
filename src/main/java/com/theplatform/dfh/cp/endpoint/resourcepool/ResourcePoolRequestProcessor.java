package com.theplatform.dfh.cp.endpoint.resourcepool;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * Request processor for the Facility Endpoint
 */
public class ResourcePoolRequestProcessor extends DataObjectRequestProcessor<ResourcePool>
{
    public ResourcePoolRequestProcessor(ObjectPersister<ResourcePool> resourcePoolObjectPersister)
    {
        super(resourcePoolObjectPersister);
    }
}
