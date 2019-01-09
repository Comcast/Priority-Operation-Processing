package com.theplatform.dfh.cp.endpoint.facility;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class CustomerRequestProcessor extends DataObjectRequestProcessor<Customer>
{
    public CustomerRequestProcessor(ObjectPersister<Customer> customerObjectPersister)
    {
        super(customerObjectPersister);
    }
}
