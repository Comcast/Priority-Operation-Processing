package com.comcast.pop.modules.jsonhelper.replacement;

import com.comcast.pop.modules.jsonhelper.JsonHelperException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
            
            contextMap.put(contextId, objectMapper.readTree(jsonData == null ? "{}" : jsonData));
        }
        catch(IOException e)
        {
            throw new JsonHelperException(String.format("Invalid jsonData passed for id: %1$s", contextId), e);
        }
    }

    /**
     * Processes the references on the specified object using the internal data map to perform replacements
     * @param object The object to map to json and perform the replacement on
     * @return A result with details about the reference replacement
     */
    public ReferenceReplacementResult processReferences(Object object)
    {
        return processReferences(objectMapper.valueToTree(object), null);
    }

    /**
     * Processes the references on the specified object using the internal data map to perform replacements
     * @param object The object to map to json and perform the replacement on
     * @param contextMaps Additional maps to combine with the internal data map to perform replacements
     * @return A result with details about the reference replacement
     */
    public ReferenceReplacementResult processReferences(Object object, List<Map<String, JsonNode>> contextMaps)
    {
        Map<String, JsonNode> combinedMap;
        if(contextMaps != null && contextMaps.size() > 0)
        {
            // create a new map with the combined references
            combinedMap = new HashMap<>(contextMap);
            contextMaps.forEach(combinedMap::putAll);
        }
        else
        {
            combinedMap = contextMap;
        }

        return jsonReferenceReplacer.replaceReferences(objectMapper.valueToTree(object), combinedMap);
    }

    protected Map<String, JsonNode> getContextMap()
    {
        return contextMap;
    }

    protected void setContextMap(Map<String, JsonNode> contextMap)
    {
        this.contextMap = contextMap;
    }

    protected void setJsonReferenceReplacer(JsonReferenceReplacer jsonReferenceReplacer)
    {
        this.jsonReferenceReplacer = jsonReferenceReplacer;
    }

    public JsonReferenceReplacer getJsonReferenceReplacer()
    {
        return jsonReferenceReplacer;
    }
}
