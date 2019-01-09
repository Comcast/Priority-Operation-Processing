package com.theplatform.dfh.cp.endpoint.facility;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class InsightRequestProcessor extends DataObjectRequestProcessor<Insight>
{
    public InsightRequestProcessor(ObjectPersister<Insight> insightObjectPersister)
    {
        super(insightObjectPersister);
    }
}
