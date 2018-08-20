package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wrapper for the JsonReferenceReplacer. Maintains a context of the items added for future use when performing reference replacement.
 */
public class JsonContext
{
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Map<String, JsonNode> contextMap;
    private JsonReferenceReplacer jsonReferenceReplacer;

    public JsonContext()
    {
        contextMap = new ConcurrentHashMap<>();
        jsonReferenceReplacer = new JsonReferenceReplacer();
    }

    public void addData(String contextId, String jsonData)
    {
        try
        {
            contextMap.put(contextId, objectMapper.readTree(jsonData));
        }
        catch(IOException e)
        {
            throw new JsonHelperException(String.format("Invalid jsonData passed for id: %1$s", contextId), e);
        }
    }

    public ReferenceReplacementResult processReferences(Object object)
    {
        return jsonReferenceReplacer.replaceReferences(objectMapper.valueToTree(object), contextMap);
    }

    protected Map<String, JsonNode> getContextMap()
    {
        return contextMap;
    }

    protected void setContextMap(Map<String, JsonNode> contextMap)
    {
        this.contextMap = contextMap;
    }

    public void setJsonReferenceReplacer(JsonReferenceReplacer jsonReferenceReplacer)
    {
        this.jsonReferenceReplacer = jsonReferenceReplacer;
    }
}
