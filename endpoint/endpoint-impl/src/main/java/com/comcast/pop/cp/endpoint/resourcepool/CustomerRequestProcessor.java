package com.comcast.pop.cp.endpoint.resourcepool;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.endpoint.base.EndpointDataObjectRequestProcessor;
import com.comcast.pop.endpoint.base.validation.RequestValidator;
import com.comcast.pop.cp.endpoint.validation.CustomerValidator;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.persistence.api.ObjectPersister;

public class CustomerRequestProcessor extends EndpointDataObjectRequestProcessor<Customer>
{
    public CustomerRequestProcessor(ObjectPersister<Customer> customerObjectPersister)
    {
        super(customerObjectPersister);
    }

    @Override
    public RequestValidator<DataObjectRequest<Customer>> getRequestValidator()
    {
        return new CustomerValidator();
    }
}
