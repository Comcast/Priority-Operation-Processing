package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TransformValidatorTest
{
    private final String CUSTOMER_ID = "theCustomer";
    private TransformValidator validator;

    @BeforeMethod
    public void setup()
    {
        validator = new TransformValidator();
    }

    @Test
    public void testValidCustomer()
    {
        validator.validate(createTransform(CUSTOMER_ID));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*The customer id must be specified on the transform.*")
    public void testInvalidCustomer()
    {
        validator.validate(createTransform(null));
    }

    private TransformRequest createTransform(String customerId)
    {
        TransformRequest transformRequest = new TransformRequest().setCustomerId(customerId);
        return transformRequest;
    }
}
