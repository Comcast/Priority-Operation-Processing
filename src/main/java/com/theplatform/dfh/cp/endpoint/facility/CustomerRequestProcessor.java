package com.theplatform.dfh.cp.endpoint.facility;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;

import java.util.UUID;

public class CustomerRequestProcessor extends BaseRequestProcessor<Customer>
{
    public CustomerRequestProcessor(ObjectPersister<Customer> agendaRequestObjectPersister)
    {
        super(agendaRequestObjectPersister);
    }

    @Override
    public ObjectPersistResponse handlePOST(Customer objectToPersist) throws BadRequestException
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
