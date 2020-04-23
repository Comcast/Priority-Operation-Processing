package com.comcast.pop.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public abstract class LambdaRequestTestBase<T extends LambdaRequest>
{
    final String OBJECT_ID = "my-Unique-Id";

    final String PAYLOAD_WITH_QUERY_FILE = "LambdaRequestPayload.json";
    final String PAYLOAD_WITH_NO_PARAMS_FILE = "LambdaRequestPayload_NoParams.json";
    final String PAYLOAD_WITH_OBJECT_FILE = "LambdaRequestPayload_Object.json";
    final String PAYLOAD_WITH_GET_OBJECT_FILE = "LambdaRequestPayload_GetObject.json";


    protected JsonNode getJSONNodeFromFile(String file) throws IOException
    {
        return JsonUtil.toJsonNode(getStringFromResourceFile(file));
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
            this.getClass().getResource(file),
            "UTF-8"
        );
    }
}
