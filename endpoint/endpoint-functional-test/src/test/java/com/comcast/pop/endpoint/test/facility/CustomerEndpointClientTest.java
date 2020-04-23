package com.comcast.pop.endpoint.test.facility;

import com.comcast.pop.endpoint.test.BaseEndpointObjectClientTest;
import com.comcast.pop.endpoint.test.factory.DataGenerator;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.endpoint.test.messages.ValidationExceptionMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CustomerEndpointClientTest extends BaseEndpointObjectClientTest<Customer>
{
    private final String UPDATED_OWNER_ID = "theUpdate";
    private static final String CLASS_FIELD = "class";

    private static final Logger logger = LoggerFactory.getLogger(CustomerEndpointClientTest.class);

    public CustomerEndpointClientTest()
    {
        super(Customer.class);
    }

    @Override
    public String getEndpointUrl()
    {
        return customerUrl;
    }

    @Override
    protected Customer getTestObject()
    {
        Customer customer = DataGenerator.generateCustomer(testCustomerId);
        return customer;
    }

    @Test
    public void testMissingTitleField()
    {
        Customer customer = DataGenerator.generateCustomer(testCustomerId);
        customer.setTitle(null);
        verifyValidationExceptionOnPersist(customer, ValidationExceptionMessage.TITLE_NOT_SPECIFIED);
    }

    @Override
    protected Customer updateTestObject(Customer object)
    {
        object.setTitle(UPDATED_OWNER_ID);
        return object;
    }

    @Override
    protected void verifyUpdatedTestObject(Customer object)
    {
        Assert.assertEquals(object.getTitle(), UPDATED_OWNER_ID);
    }

    @Override
    protected void verifyCreatedTestObject(Customer createdObject, Customer testObject)
    {
        assertEqual(createdObject, testObject);
    }
}

