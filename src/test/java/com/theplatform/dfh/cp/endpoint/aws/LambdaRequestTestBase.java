package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public abstract class LambdaRequestTestBase<T extends LambdaRequest>
{
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
