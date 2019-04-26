package com.theplatform.dfh.cp.endpoint.facility.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.theplatform.dfh.cp.api.facility.Customer;

import java.util.Date;
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
    public Date getUpdatedTime()
    {
        return super.getUpdatedTime();
    }

    @Override
    public Date getAddedTime()
    {
        return super.getAddedTime();
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
        return super.getCustomerId();
    }

    @Override
    public String getResourcePoolId()
    {
        return super.getResourcePoolId();
    }

    @Override
    public String getCid()
    {
        return super.getCid();
    }
}
