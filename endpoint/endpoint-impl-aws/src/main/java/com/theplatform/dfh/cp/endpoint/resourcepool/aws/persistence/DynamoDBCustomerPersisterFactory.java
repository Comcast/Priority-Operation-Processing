package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.Customer;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

/**
 */
public class DynamoDBCustomerPersisterFactory extends DynamoDBConvertedPersisterFactory<Customer, PersistentCustomer>
{
    protected static final TableIndexes tableIndexes = new TableIndexes().withIndex("resourcepoolid_index", "resourcePoolId");

    public DynamoDBCustomerPersisterFactory()
    {
        super("id", Customer.class, new PersistentCustomerConverter(), tableIndexes);
    }
}
