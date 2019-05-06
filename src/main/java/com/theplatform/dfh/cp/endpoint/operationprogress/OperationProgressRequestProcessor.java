package com.theplatform.dfh.cp.endpoint.operationprogress;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * OperationProgress specific RequestProcessor
 */
public class OperationProgressRequestProcessor extends EndpointDataObjectRequestProcessor<OperationProgress>
{
    public OperationProgressRequestProcessor(ObjectPersister<OperationProgress> operationProgressRequestObjectPersister)
    {
        super(operationProgressRequestObjectPersister);
    }
}