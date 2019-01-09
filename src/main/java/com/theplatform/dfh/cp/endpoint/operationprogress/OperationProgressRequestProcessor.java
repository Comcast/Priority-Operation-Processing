package com.theplatform.dfh.cp.endpoint.operationprogress;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

import javax.xml.crypto.Data;

/**
 * OperationProgress specific RequestProcessor
 */
public class OperationProgressRequestProcessor extends DataObjectRequestProcessor<OperationProgress>
{
    public OperationProgressRequestProcessor(ObjectPersister<OperationProgress> operationProgressRequestObjectPersister)
    {
        super(operationProgressRequestObjectPersister);
    }
}