package com.theplatform.dfh.cp.endpoint.resourcepool.aws.persistence;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.comcast.pop.api.facility.Customer;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.ListOperationsConverter;
import com.theplatform.dfh.cp.endpoint.persistence.DateConverter;

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
    @DynamoDBTypeConverted(converter = DateConverter.class)
    public Date getUpdatedTime()
    {
        return super.getUpdatedTime();
    }

    @Override
    @DynamoDBTypeConverted(converter = DateConverter.class)
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
