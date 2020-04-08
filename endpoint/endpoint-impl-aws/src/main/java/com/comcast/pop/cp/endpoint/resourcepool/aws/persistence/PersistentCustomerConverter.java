package com.comcast.pop.cp.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentCustomerConverter extends DynamoDBPersistentObjectConverter<Customer, PersistentCustomer>
{
    public PersistentCustomerConverter()
    {
        super(Customer.class, PersistentCustomer.class);
    }
}
