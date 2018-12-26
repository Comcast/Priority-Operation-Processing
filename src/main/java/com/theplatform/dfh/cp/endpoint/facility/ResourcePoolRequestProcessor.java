package com.theplatform.dfh.cp.endpoint.facility;

import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;

import java.util.UUID;

/**
 * Request processor for the Facility Endpoint
 */
public class ResourcePoolRequestProcessor extends BaseRequestProcessor<ResourcePool>
{
    public ResourcePoolRequestProcessor(ObjectPersister<ResourcePool> agendaRequestObjectPersister)
    {
        super(agendaRequestObjectPersister);
    }

    @Override
    public ObjectPersistResponse handlePOST(ResourcePool objectToPersist) throws BadRequestException
    {
        try
        {
            String objectId = UUID.randomUUID().toString();
            objectToPersist.setId(objectId);
            objectPersister.persist(objectToPersist);
            return new ObjectPersistResponse(objectId);
        }
        catch (PersistenceException e)
        {
            throw new BadRequestException(e);
        }
    }
}
