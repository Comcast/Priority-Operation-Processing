package com.theplatform.dfh.cp.endpoint.operationprogress;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;

import java.util.UUID;

/**
 * OperationProgress specific RequestProcessor
 */
public class OperationProgressRequestProcessor extends BaseRequestProcessor<OperationProgress>
{
    public OperationProgressRequestProcessor(ObjectPersister<OperationProgress> operationProgressRequestObjectPersister)
    {
        super(operationProgressRequestObjectPersister);
    }

    @Override
    public ObjectPersistResponse handlePOST(OperationProgress objectToPersist) throws BadRequestException
    {
        String objectId = objectToPersist.getId();
        if (objectId == null)
        {
            objectId = UUID.randomUUID().toString();
            objectToPersist.setId(objectId);
        }
        try
        {
            objectPersister.persist(objectToPersist);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException("Unable to create object", e);
        }
        return new ObjectPersistResponse(objectId);
    }
}