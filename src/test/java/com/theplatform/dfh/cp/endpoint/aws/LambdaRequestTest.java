package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.persistence.api.query.Query;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;


public class LambdaRequestTest
{

    @Test
    public void testGetQueriesNullMap()
    {
        LambdaRequest request = new LambdaRequest(null);
        Assert.assertNull(request.getQueries());
    }
    @Test
    public void testGetQueriesBadRequest() throws IOException
    {
        JsonNode payload = JsonUtil.toJsonNode(getStringFromResourceFile("LambdaRequestPayload.json"));

        LambdaRequest request = new LambdaRequest(payload);
        Assert.assertNotNull(request.getQueries());
        Assert.assertEquals(request.getQueries().size(), 2);
        Assert.assertEquals(((Query)request.getQueries().get(0)).getValue(), "myTitle");
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
            this.getClass().getResource(file),
            "UTF-8"
        );
    }
}
