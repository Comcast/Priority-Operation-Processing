package com.theplatform.dfh.cp.endpoint.facility.aws.persistence;

import com.theplatform.dfh.cp.api.facility.Customer;
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
