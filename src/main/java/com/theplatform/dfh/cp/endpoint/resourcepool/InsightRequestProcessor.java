package com.theplatform.dfh.cp.endpoint.resourcepool;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.*;
import com.theplatform.dfh.cp.endpoint.validation.InsightValidator;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class InsightRequestProcessor extends DataObjectRequestProcessor<Insight>
{
    private AnyMatchVisibilityFilter globalObjectVisibilityFilter =
        new AnyMatchVisibilityFilter()
            .withFilter(new CustomerVisibilityFilter())
            .withFilter(new AllowedCustomerVisibiltyFilter());

    public InsightRequestProcessor(ObjectPersister<Insight> insightObjectPersister)
    {
        super(insightObjectPersister);
        setVisibilityFilter(globalObjectVisibilityFilter);
    }

    @Override
    public RequestValidator<DataObjectRequest<Insight>> getRequestValidator()
    {
        return new InsightValidator();
    }

    public VisibilityFilter getVisibilityFilter()
    {
        return globalObjectVisibilityFilter;
    }
}
