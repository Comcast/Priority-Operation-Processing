package com.comcast.pop.cp.endpoint.resourcepool.aws.persistence;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.comcast.pop.persistence.aws.dynamodb.TableIndexes;

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
