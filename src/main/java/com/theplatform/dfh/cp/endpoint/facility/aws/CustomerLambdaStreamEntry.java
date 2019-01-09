package com.theplatform.dfh.cp.endpoint.facility.aws;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.LambdaDataObjectRequest;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.CustomerRequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.PersistentCustomerConverter;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class CustomerLambdaStreamEntry extends BaseAWSLambdaStreamEntry<Customer>
{
    private static final TableIndexes tableIndexes = null;

    public CustomerLambdaStreamEntry()
    {
        super(
            Customer.class,
            new DynamoDBConvertedPersisterFactory<>("id", Customer.class,
                new PersistentCustomerConverter(), tableIndexes)
        );
    }

    @Override
    protected RequestProcessor getRequestProcessor(LambdaDataObjectRequest<Customer> lambdaDataObjectRequest, ObjectPersister<Customer> objectPersister)
    {
        return new CustomerRequestProcessor(objectPersister);
    }


    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.CUSTOMER;
    }
}
