package com.theplatform.dfh.cp.endpoint.resourcepool;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.endpoint.base.EndpointDataObjectRequestProcessor;
import com.comcast.pop.endpoint.base.validation.RequestValidator;
import com.comcast.pop.endpoint.base.visibility.*;
import com.theplatform.dfh.cp.endpoint.validation.InsightValidator;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class InsightRequestProcessor extends EndpointDataObjectRequestProcessor<Insight>
{
    private static final AnyMatchVisibilityFilter globalObjectReadVisibilityFilter =
        new AnyMatchVisibilityFilter()
            .withFilter(new GlobalObjectVisibilityFilter())
            .withFilter(new CustomerVisibilityFilter())
            .withFilter(new AllowedCustomerVisibiltyFilter());

    public InsightRequestProcessor(ObjectPersister<Insight> insightObjectPersister)
    {
        super(insightObjectPersister);
        //allow global and allowed customers for READS
        setVisibilityFilter(VisibilityMethod.GET, globalObjectReadVisibilityFilter);
    }

    @Override
    protected Insight defaultFieldsOnCreate(Insight object)
    {
        if(object.isGlobal() == null) object.setIsGlobal(false);
        if(object.getQueueSize() == null) object.setQueueSize(0);
        return object;
    }

    @Override
    public RequestValidator<DataObjectRequest<Insight>> getRequestValidator()
    {
        return new InsightValidator();
    }

    public static AnyMatchVisibilityFilter getDefaultObjectReadVisibilityFilter()
    {
        return globalObjectReadVisibilityFilter;
    }

}
