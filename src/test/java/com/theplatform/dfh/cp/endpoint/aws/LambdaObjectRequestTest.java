package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.endpoint.api.BadRequestException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class LambdaObjectRequestTest extends LambdaRequestTestBase
{
    private final String OBJECT_ID = "my-Unique-Id";

    private final String PAYLOAD_WITH_OBJECT_FILE = "LambdaRequestPayload_Object.json";
    private final String PAYLOAD_WITH_GET_OBJECT_FILE = "LambdaRequestPayload_GetObject.json";

    @Test
    public void testGETObjectIdFromPath() throws IOException
    {
        LambdaObjectRequest<AgendaProgress> lambdaObjectRequest = new LambdaObjectRequest<>(getJSONNodeFromFile(PAYLOAD_WITH_GET_OBJECT_FILE), AgendaProgress.class);
        Assert.assertEquals(lambdaObjectRequest.getDataObjectId(), OBJECT_ID);
        // TODO: this handling is not yet defined
        Assert.assertNull(lambdaObjectRequest.getDataObject());
    }

    @Test(expectedExceptions = BadRequestException.class)
    public void testGETObjectJsonException() throws IOException
    {
        LambdaObjectRequest<AgendaProgress> lambdaObjectRequest = new LambdaObjectRequest<>(getJSONNodeFromFile(PAYLOAD_WITH_GET_OBJECT_FILE), AgendaProgress.class);
        Assert.assertEquals(lambdaObjectRequest.getDataObjectId(), OBJECT_ID);
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        doThrow(new IOException()).when(mockObjectMapper).readValue(any(String.class), any(Class.class));
        lambdaObjectRequest.setObjectMapper(mockObjectMapper);
        lambdaObjectRequest.getDataObject();
    }

    @Test
    public void testGETObjectFromData() throws IOException
    {
        LambdaObjectRequest<AgendaProgress> lambdaObjectRequest = new LambdaObjectRequest<>(getJSONNodeFromFile(PAYLOAD_WITH_OBJECT_FILE), AgendaProgress.class);
        Assert.assertEquals(lambdaObjectRequest.getDataObjectId(), OBJECT_ID);
        Assert.assertNotNull(lambdaObjectRequest.getDataObject());
    }
}
