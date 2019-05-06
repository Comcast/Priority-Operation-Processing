package com.theplatform.dfh.cp.endpoint.facility;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class CustomerRequestProcessor extends EndpointDataObjectRequestProcessor<Customer>
{
    public CustomerRequestProcessor(ObjectPersister<Customer> customerObjectPersister)
    {
        super(customerObjectPersister);
    }
}
