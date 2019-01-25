package com.theplatform.dfh.cp.endpoint.facility.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.theplatform.dfh.cp.api.facility.Customer;

import java.util.List;

/**
 */
public class PersistentCustomer extends Customer
{

    @Override
    @DynamoDBHashKey
    public String getId()
    {
        return super.getId();
    }

    @Override
    public String getTitle()
    {
        return super.getTitle();
    }

    @Override
    public String getBillingCode()
    {
        return super.getBillingCode();
    }

    @Override
    public String getCustomerId()
    {
        return super.getId();
    }

    @Override
    public String getResourcePoolId()
    {
        return super.getResourcePoolId();
    }
}
