package com.theplatform.dfh.cp.endpoint.resourcepool;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.*;
import com.theplatform.dfh.cp.endpoint.validation.InsightValidator;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
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

    public static InsightRequestProcessor getServiceInstance(ObjectPersister<Insight> insightObjectPersister, ServiceRequest<Insight> serviceRequest)
    {
        InsightRequestProcessor processor = new InsightRequestProcessor(insightObjectPersister);
        //the service needs extra visibility checking.
        //get current visibilty filter
        AllMatchVisibilityFilter allMatchVisibilityFilter = new AllMatchVisibilityFilter()
            .withFilter(globalObjectReadVisibilityFilter)
            .withFilter(new ServiceRequestVisibilityFilter(serviceRequest));
        processor.setVisibilityFilter(VisibilityMethod.GET, allMatchVisibilityFilter);
        return processor;
    }



}
