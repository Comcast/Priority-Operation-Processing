package com.theplatform.dfh.cp.modules.jsonhelper;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 *
 */
public class JsonHelper
{
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public JsonHelper()
    {

    }

    /**
     * Gets a node via JSON pointer from the Job and unmarshals it to the specified class
     * @param ref The json pointer
     * @param clazz The class to unmarshal to
     * @param <T>
     * @return The object or null (if not found)
     */
    public <T> T getObjectFromRef(JsonNode rootNode, String ref, Class<T> clazz)
    {
        JsonNode jsonNode = rootNode.at(ref);
        if(jsonNode.isMissingNode()) return null;
        try
        {
            return objectMapper.treeToValue(jsonNode, clazz);
        }
        catch(JsonProcessingException e)
        {
            throw new JsonHelperException(String.format("Failed to map ref %1$s to object %2$s.", ref, clazz.getSimpleName()), e);
        }
    }

    /**
     * Performs a mapping from a Map to the specified class
     * @param map The map to convert from
     * @param clazz The target object type
     * @param <T> The template object type
     * @return The object
     */
    public <T> T getObjectFromMap(Map<String, Object> map, Class<T> clazz)
    {
        return objectMapper.convertValue(map, clazz);
    }

    /**
     * Unmarshals the input string to the specified class
     * @param json The json string to convert
     * @param clazz The class to unmarshal to
     * @param <T>
     * @return The object or null (if not found)
     */
    public <T> T getObjectFromString(String json, Class<T> clazz)
    {
        try
        {
            return objectMapper.readValue(json, clazz);
        }
        catch(IOException e)
        {
            throw new JsonHelperException(String.format("Failed to map json to object %2$s.", clazz.getSimpleName(), e));
        }
    }

    /**
     * Gets the JSON representation of the specified object
     * @param object Object to get the JSON string of
     * @return The resulting String
     */
    public String getJSONString(Object object)
    {
        try
        {
            return objectMapper.writeValueAsString(object);
        }
        catch(JsonProcessingException e)
        {
            throw new JsonHelperException("Failed to map object to json.", e);
        }
    }

    /**
     * Gets the JSON representation of the specified object
     * @param object Object to get the JSON string of
     * @return The resulting String
     */
    public String getPrettyJSONString(Object object)
    {
        try
        {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        }
        catch(JsonProcessingException e)
        {
            throw new JsonHelperException("Failed to map object to json.", e);
        }
    }

    /**
     * Sets the value of the specified node to the string indicated
     * @param rootNode The root JsonNode
     * @param jsonPtrExpr The JsonPtr to the field to set
     * @param value The value to set on the node
     */
    public void setNodeValue(JsonNode rootNode, String jsonPtrExpr, String value)
    {
        if(StringUtils.isBlank(jsonPtrExpr)
            || StringUtils.equals(jsonPtrExpr,"/")
            || StringUtils.endsWith(jsonPtrExpr, "/"))
        {
            throw new JsonHelperException("jsonPtrExpr must be a subnode.");
        }

        JsonPointer valueNodePointer = JsonPointer.compile(jsonPtrExpr);
        JsonPointer containerPointer = valueNodePointer.head();
        JsonNode parentJsonNode = rootNode.at(containerPointer);

        if (parentJsonNode.isMissingNode() || !parentJsonNode.isObject())
        {
            throw new JsonHelperException(String.format("Failed to find node at ref %1$s", jsonPtrExpr));
        }

        ((ObjectNode) parentJsonNode)
            .put(
                jsonPtrExpr.substring(jsonPtrExpr.lastIndexOf('/') + 1),
                value);
    }
}
