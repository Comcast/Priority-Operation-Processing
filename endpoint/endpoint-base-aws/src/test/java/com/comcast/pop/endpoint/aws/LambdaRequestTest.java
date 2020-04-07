package com.comcast.pop.endpoint.aws;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class LambdaRequestTest extends LambdaRequestTestBase
{
    @Test
    public void testGetQueriesNoParams() throws IOException
    {
        LambdaRequest request = new LambdaRequest(getJSONNodeFromFile(PAYLOAD_WITH_NO_PARAMS_FILE));
        Assert.assertNull(request.getRequestParamMap());
    }

    @Test
    public void testGetRequestParamMap() throws IOException
    {
        LambdaRequest request = new LambdaRequest(getJSONNodeFromFile(PAYLOAD_WITH_QUERY_FILE));
        Assert.assertNotNull(request.getRequestParamMap());
        Assert.assertEquals(request.getRequestParamMap().get("byTitle"), "myTitle");
        Assert.assertEquals(request.getRequestParamMap().get("byGuid"), "dfoweruwo3j2p2");
    }
}
