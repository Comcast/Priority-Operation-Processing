package com.theplatform.dfh.cp.jsonhelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

// TODO: exception handling, eval if this is anything more than prototype code

/**
 *
 */
public class JsonHelper
{
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Gets a node via JSON pointer from the Job and unmarshals it to the specified class
     * @param ref The json pointer
     * @param clazz The class to unmarshal to
     * @param <T>
     * @return The object or null (if not found)
     */
    public static <T> T getObjectFromRef(JsonNode rootNode, String ref, Class clazz)
    {
        JsonNode jsonNode = rootNode.at(ref);
        if(jsonNode.isMissingNode()) return null;
        try
        {
            return (T)objectMapper.treeToValue(jsonNode, clazz);
        }
        catch(JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unmarshals the input string to the specified class
     * @param json The json string to convert
     * @param clazz The class to unmarshal to
     * @param <T>
     * @return The object or null (if not found)
     */
    public static <T> T getObjectFromString(String json, Class clazz)
    {
        try
        {
            return (T)objectMapper.readValue(json, clazz);
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the JSON representation of the specified object
     * @param object Object to get the JSON string of
     * @return The resulting String
     */
    public static String getJSONString(Object object)
    {
        try
        {
            return objectMapper.writeValueAsString(object);
        }
        catch(JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the JSON representation of the specified object
     * @param object Object to get the JSON string of
     * @return The resulting String
     */
    public static String getPrettyJSONString(Object object)
    {
        try
        {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        }
        catch(JsonProcessingException e)
        {
            throw new RuntimeException(e);
        }
    }
}
