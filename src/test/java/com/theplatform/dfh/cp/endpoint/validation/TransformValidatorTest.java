package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.api.input.InputStream;
import com.theplatform.dfh.cp.api.input.InputStreams;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

public class TransformValidatorTest extends BaseValidatorTest<TransformRequest>
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
        validator.validatePOST(createRequest(createTransform(CUSTOMER_ID)));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*The customer id must be specified on the transform.*")
    public void testInvalidCustomer()
    {
        validator.validatePOST(createRequest(createTransform(null)));
    }

    @Test
    public void testValidInputStreamReference()
    {
        TransformRequest transformRequest = createTransform(CUSTOMER_ID);

        InputStream inputStream = new InputStream();
        inputStream.setInputRef("/inputs/0");
        InputStreams inputStreams = new InputStreams();
        inputStreams.setVideo(Collections.singletonList(inputStream));
        transformRequest.setInputStreams(inputStreams);

        InputFileResource inputFileResource = new InputFileResource();
        transformRequest.setInputs(Collections.singletonList(inputFileResource));

        validator.validatePOST(createRequest(transformRequest));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid stream reference found.*")
    public void testInvalidInputStreamReference()
    {
        TransformRequest transformRequest = createTransform(CUSTOMER_ID);

        InputStream inputStream = new InputStream();
        inputStream.setInputRef("/inputs/0");
        InputStreams inputStreams = new InputStreams();
        inputStreams.setVideo(Collections.singletonList(inputStream));
        transformRequest.setInputStreams(inputStreams);

        validator.validatePOST(createRequest(transformRequest));
    }

    private TransformRequest createTransform(String customerId)
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setCustomerId(customerId);
        return transformRequest;
    }
}
