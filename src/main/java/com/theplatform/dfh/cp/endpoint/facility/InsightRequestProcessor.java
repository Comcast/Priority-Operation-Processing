package com.theplatform.dfh.cp.endpoint.facility;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;

import java.util.UUID;

public class InsightRequestProcessor extends BaseRequestProcessor<Insight>
{
    public InsightRequestProcessor(ObjectPersister<Insight> insightObjectPersister)
    {
        super(insightObjectPersister);
    }
}
