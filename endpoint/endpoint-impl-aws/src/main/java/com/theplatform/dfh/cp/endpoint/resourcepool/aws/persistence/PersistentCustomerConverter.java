package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.Customer;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentCustomerConverter extends DynamoDBPersistentObjectConverter<Customer, PersistentCustomer>
{
    public PersistentCustomerConverter()
    {
        super(Customer.class, PersistentCustomer.class);
    }
}
