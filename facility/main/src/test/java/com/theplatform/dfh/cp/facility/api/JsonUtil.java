package com.theplatform.dfh.cp.facility.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

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

    public static Object toObject(String jsonString, Class clazz) throws IOException
    {
        return objectMapper.readValue(jsonString.getBytes(), clazz);
    }

    public static ObjectMapper getObjectMapper()
    {
        return objectMapper;
    }
}
