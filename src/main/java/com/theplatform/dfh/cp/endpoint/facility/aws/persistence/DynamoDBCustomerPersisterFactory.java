package com.theplatform.dfh.cp.endpoint.facility.aws.persistence;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;

/**
 */
public class DynamoDBCustomerPersisterFactory extends DynamoDBConvertedPersisterFactory<Customer>
{
    public DynamoDBCustomerPersisterFactory()
    {
        super("id", Customer.class, new PersistentCustomerConverter(), null);
    }
}
