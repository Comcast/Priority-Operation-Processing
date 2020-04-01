package com.cts.fission.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 */
public class JsonUtil
{
    private static ObjectMapper objectMapper = new ObjectMapper();

    static
    {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static String toJson(Object obj) throws JsonProcessingException
    {
        return objectMapper.writeValueAsString(obj);
    }

    public static JsonNode toJsonNode(String jsonString) throws IOException
    {
        return objectMapper.readTree(jsonString);
    }

    public static<T> T toObject(String jsonString, Class<T> clazz) throws IOException
    {
        return objectMapper.readValue(jsonString.getBytes(), clazz);
    }

    public static<T> T toObjectFromFile(String file, Class<T> clazz) throws IOException
    {
        return objectMapper.readValue(getStringFromResourceFile(file, JsonUtil.class), clazz);
    }

    public static<T> T toObjectFromFile(String file, Class<T> clazz, Class resourceClass) throws IOException
    {
        return objectMapper.readValue(getStringFromResourceFile(file, resourceClass), clazz);
    }

    public static ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }


    public static String getStringFromResourceFile(String file, Class resourceClass) throws IOException
    {
        return IOUtils.toString(
            resourceClass.getResource(file),
            "UTF-8"
        );
    }
}
