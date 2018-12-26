package com.theplatform.dfh.cp.endpoint.facility;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class CustomerRequestProcessor extends BaseRequestProcessor<Customer>
{
    public CustomerRequestProcessor(ObjectPersister<Customer> customerObjectPersister)
    {
        super(customerObjectPersister);
    }
}
