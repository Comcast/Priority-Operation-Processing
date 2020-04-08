package com.comcast.pop.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonReplacementTestBase
{
    protected ObjectMapper objectMapper = new ObjectMapper();

    protected Map<String,JsonNode> getParameterMap(JsonNode nodeParameters)
    {
        Map<String, JsonNode> parameterMap = new HashMap<>();

        nodeParameters.fields().forEachRemaining(entry ->
            parameterMap.put(entry.getKey(), entry.getValue())
        );

        return parameterMap;
    }

    protected Map<String, JsonNode> getSingleTestParamMap(String parameterName, JsonNode jsonNode)
    {
        Map<String, JsonNode> paramMap = new HashMap<>();
        if(parameterName != null)
        {
            paramMap.put(parameterName, jsonNode);
        }
        return paramMap;
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
            this.getClass().getResource(file),
            "UTF-8"
        );
    }

    protected JsonNode getJsonNodeFromFile(String file) throws IOException
    {
        return objectMapper.readTree(getStringFromResourceFile(file));
    }
}
