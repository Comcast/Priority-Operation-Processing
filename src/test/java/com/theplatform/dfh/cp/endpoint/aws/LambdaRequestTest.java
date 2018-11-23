package com.theplatform.dfh.cp.endpoint.aws;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class LambdaRequestTest extends LambdaRequestTestBase
{
    private final String PAYLOAD_WITH_QUERY_FILE = "LambdaRequestPayload.json";
    private final String PAYLOAD_WITH_NO_PARAMS_FILE = "LambdaRequestPayload_NoParams.json";

    @Test
    public void testGetQueriesNullMap()
    {
        LambdaRequest request = new LambdaRequest(null);
        Assert.assertNull(request.getQueries());
        Assert.assertNull(request.getJsonNode());
    }

    @Test
    public void testGetQueries() throws IOException
    {
        LambdaRequest request = new LambdaRequest(getJSONNodeFromFile(PAYLOAD_WITH_QUERY_FILE));
        Assert.assertNotNull(request.getQueries());
        Assert.assertEquals(request.getQueries().size(), 2);
        Assert.assertEquals((request.getQueries().get(0)).getValue(), "myTitle");
        Assert.assertEquals((request.getQueries().get(1)).getValue(), "dfoweruwo3j2p2");
    }

    @Test
    public void testGetQueriesNoParams() throws IOException
    {
        LambdaRequest request = new LambdaRequest(getJSONNodeFromFile(PAYLOAD_WITH_NO_PARAMS_FILE));
        Assert.assertNull(request.getQueries());
    }
}
