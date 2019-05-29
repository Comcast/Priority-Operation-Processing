package com.theplatform.dfh.cp.endpoint.validation;

import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.input.InputFileResource;
import com.theplatform.dfh.cp.api.input.InputStream;
import com.theplatform.dfh.cp.api.input.InputStreams;
import com.theplatform.dfh.cp.api.output.OutputFileResource;
import com.theplatform.dfh.cp.api.output.OutputStream;
import com.theplatform.dfh.cp.api.output.OutputStreams;
import com.theplatform.dfh.endpoint.api.ValidationException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TransformValidatorTest extends BaseValidatorTest<TransformRequest>
{
    private static final String DEFAULT_INPUT_REF = "/inputs/0";
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

    ///
    /// InputStreams
    ///

    @Test
    public void testValidVideoInputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithInputStreams(CUSTOMER_ID);
        transformRequest.getInputStreams().setVideo(Collections.singletonList(createInputStream(DEFAULT_INPUT_REF)));
        callValidatePostWithInputFileResource(transformRequest);
    }

    @Test
    public void testValidAudioInputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithInputStreams(CUSTOMER_ID);
        transformRequest.getInputStreams().setAudio(Collections.singletonList(createInputStream(DEFAULT_INPUT_REF)));
        callValidatePostWithInputFileResource(transformRequest);
    }

    @Test
    public void testValidTextInputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithInputStreams(CUSTOMER_ID);
        transformRequest.getInputStreams().setText(Collections.singletonList(createInputStream(DEFAULT_INPUT_REF)));
        callValidatePostWithInputFileResource(transformRequest);
    }

    @Test
    public void testValidImageInputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithInputStreams(CUSTOMER_ID);
        transformRequest.getInputStreams().setImage(Collections.singletonList(createInputStream(DEFAULT_INPUT_REF)));
        callValidatePostWithInputFileResource(transformRequest);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid stream reference found.*")
    public void testInvalidVideoInputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithInputStreams(CUSTOMER_ID);
        transformRequest.getInputStreams().setVideo(Collections.singletonList(createInputStream(DEFAULT_INPUT_REF)));
        validator.validatePOST(createRequest(transformRequest));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid stream reference found.*")
    public void testInvalidAudioInputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithInputStreams(CUSTOMER_ID);
        transformRequest.getInputStreams().setAudio(Collections.singletonList(createInputStream(DEFAULT_INPUT_REF)));
        validator.validatePOST(createRequest(transformRequest));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid stream reference found.*")
    public void testInvalidTextInputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithInputStreams(CUSTOMER_ID);
        transformRequest.getInputStreams().setText(Collections.singletonList(createInputStream(DEFAULT_INPUT_REF)));
        validator.validatePOST(createRequest(transformRequest));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid stream reference found.*")
    public void testInvalidImageInputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithInputStreams(CUSTOMER_ID);
        transformRequest.getInputStreams().setImage(Collections.singletonList(createInputStream(DEFAULT_INPUT_REF)));
        validator.validatePOST(createRequest(transformRequest));
    }

    ///
    /// OutputStreams
    ///

    @Test
    public void testValidVideoOutputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithOutputStreams(CUSTOMER_ID);
        transformRequest.getOutputStreams().setVideo(Collections.singletonList(createOutputStream(DEFAULT_INPUT_REF)));
        callValidatePostWithInputFileResource(transformRequest);
    }

    @Test
    public void testValidAudioOutputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithOutputStreams(CUSTOMER_ID);
        transformRequest.getOutputStreams().setAudio(Collections.singletonList(createOutputStream(DEFAULT_INPUT_REF)));
        callValidatePostWithInputFileResource(transformRequest);
    }

    @Test
    public void testValidTextOutputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithOutputStreams(CUSTOMER_ID);
        transformRequest.getOutputStreams().setText(Collections.singletonList(createOutputStream(DEFAULT_INPUT_REF)));
        callValidatePostWithInputFileResource(transformRequest);
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid stream reference found.*")
    public void testInvalidVideoOutputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithOutputStreams(CUSTOMER_ID);
        transformRequest.getOutputStreams().setVideo(Collections.singletonList(createOutputStream(DEFAULT_INPUT_REF)));
        validator.validatePOST(createRequest(transformRequest));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid stream reference found.*")
    public void testInvalidAudioOutputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithOutputStreams(CUSTOMER_ID);
        transformRequest.getOutputStreams().setAudio(Collections.singletonList(createOutputStream(DEFAULT_INPUT_REF)));
        validator.validatePOST(createRequest(transformRequest));
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Invalid stream reference found.*")
    public void testInvalidTextOutputStreamReference()
    {
        TransformRequest transformRequest = createTransformWithOutputStreams(CUSTOMER_ID);
        transformRequest.getOutputStreams().setText(Collections.singletonList(createOutputStream(DEFAULT_INPUT_REF)));
        validator.validatePOST(createRequest(transformRequest));
    }


    @DataProvider
    public Object[][] missingOutputStreamRefsProvider()
    {
        return new Object[][]
            {
                {null},
                {new ArrayList<>() }
            };
    }

    @Test(expectedExceptions = ValidationException.class, expectedExceptionsMessageRegExp = ".*Transform Request must include a value for required outputStreamRefs.*",
    dataProvider = "missingOutputStreamRefsProvider")
    public void testMissingOutputStreamRefs(List<String> outputStreamRefs)
    {
        TransformRequest transformRequest = createTransform(CUSTOMER_ID);
        OutputFileResource outputFileResource = new OutputFileResource();
        outputFileResource.setUrl("http://theplatform.com/file.mp4");
        outputFileResource.setType("video");
        outputFileResource.setOutputStreamRefs(outputStreamRefs);
        transformRequest.setOutputs(Collections.singletonList(outputFileResource));
        validator.validatePOST(createRequest(transformRequest));
    }

    @Test
    public void testValidVideoOutputReference()
    {
        TransformRequest transformRequest = createTransformWithOutputStreams(CUSTOMER_ID);
        OutputFileResource outputFileResource = new OutputFileResource();
        outputFileResource.setUrl("http://theplatform.com/file.mp4");
        outputFileResource.setType("video");
        outputFileResource.setOutputStreamRefs(Collections.singletonList("/outputStreams/video/0"));
        transformRequest.setOutputs(Collections.singletonList(outputFileResource));
        transformRequest.getOutputStreams().setVideo(Collections.singletonList(createOutputStream(DEFAULT_INPUT_REF)));
        callValidatePostWithInputFileResource(transformRequest);
    }

    private void callValidatePostWithInputFileResource(TransformRequest transformRequest)
    {
        InputFileResource inputFileResource = new InputFileResource();
        transformRequest.setInputs(Collections.singletonList(inputFileResource));
        validator.validatePOST(createRequest(transformRequest));
    }

    private OutputStream createOutputStream(String ref)
    {
        OutputStream outputStream = new OutputStream();
        outputStream.setOutputRef(ref);
        return outputStream;
    }

    private InputStream createInputStream(String ref)
    {
        InputStream inputStream = new InputStream();
        inputStream.setInputRef(ref);
        return inputStream;
    }

    private TransformRequest createTransformWithOutputStreams(String customerId)
    {
        TransformRequest transformRequest = createTransform(customerId);
        transformRequest.setOutputStreams(new OutputStreams());
        return transformRequest;
    }

    private TransformRequest createTransformWithInputStreams(String customerId)
    {
        TransformRequest transformRequest = createTransform(customerId);
        transformRequest.setInputStreams(new InputStreams());
        return transformRequest;
    }

    private TransformRequest createTransform(String customerId)
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setCustomerId(customerId);
        return transformRequest;
    }
}
