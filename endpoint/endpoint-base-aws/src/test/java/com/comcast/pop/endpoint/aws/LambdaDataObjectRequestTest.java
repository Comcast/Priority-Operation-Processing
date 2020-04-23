package com.comcast.pop.endpoint.aws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.endpoint.api.BadRequestException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class LambdaDataObjectRequestTest extends LambdaRequestTestBase
{
    @Test
    public void testGetQueriesNullMap()
    {
        LambdaDataObjectRequest<AgendaProgress> request = new LambdaDataObjectRequest<>(null, AgendaProgress.class);
        Assert.assertNull(request.getQueries());
        Assert.assertNull(request.getJsonNode());
    }

    @Test
    public void testGetQueries() throws IOException
    {
        LambdaDataObjectRequest<AgendaProgress> request = new LambdaDataObjectRequest<>(getJSONNodeFromFile(PAYLOAD_WITH_QUERY_FILE), AgendaProgress.class);
        Assert.assertNotNull(request.getQueries());
        Assert.assertEquals(request.getQueries().size(), 2);

        // convert the list to a map for convenience
        Map<String, String> queryStringMap = request.getQueries().stream().collect(
            Collectors.toMap(q-> q.getField().name(), q-> q.getValue().toString())
        );
        Assert.assertEquals(queryStringMap.get("Title"), "myTitle");
        Assert.assertEquals(queryStringMap.get("Guid"), "dfoweruwo3j2p2");
    }

    @Test
    public void testGETObjectIdFromPath() throws IOException
    {
        LambdaDataObjectRequest<AgendaProgress> lambdaObjectRequest = new LambdaDataObjectRequest<>(getJSONNodeFromFile(PAYLOAD_WITH_GET_OBJECT_FILE), AgendaProgress.class);
        Assert.assertEquals(lambdaObjectRequest.getId(), OBJECT_ID);
        // TODO: this handling is not yet defined
        Assert.assertNull(lambdaObjectRequest.getDataObject());
    }

    @Test(expectedExceptions = BadRequestException.class, enabled = false)
    public void testGETObjectJsonException() throws IOException
    {
        LambdaDataObjectRequest<AgendaProgress> lambdaObjectRequest = new LambdaDataObjectRequest<>(getJSONNodeFromFile(PAYLOAD_WITH_GET_OBJECT_FILE), AgendaProgress.class);
        Assert.assertEquals(lambdaObjectRequest.getId(), OBJECT_ID);
        ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
        doThrow(new IOException()).when(mockObjectMapper).readValue(any(String.class), any(Class.class));
        lambdaObjectRequest.setObjectMapper(mockObjectMapper);
        lambdaObjectRequest.getDataObject();
    }

    @Test
    public void testGETObjectFromData() throws IOException
    {
        LambdaDataObjectRequest<AgendaProgress> lambdaObjectRequest = new LambdaDataObjectRequest<>(getJSONNodeFromFile(PAYLOAD_WITH_OBJECT_FILE), AgendaProgress.class);
        Assert.assertEquals(lambdaObjectRequest.getId(), OBJECT_ID);
        Assert.assertNotNull(lambdaObjectRequest.getDataObject());
    }
}
