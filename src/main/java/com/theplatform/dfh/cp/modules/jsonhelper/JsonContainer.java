package com.theplatform.dfh.cp.modules.jsonhelper;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;

public class JsonContainer
{
    private JsonHelper jsonHelper;
    private final JsonNode rootNode;

    public JsonContainer(String jsonString) throws IOException
    {
        jsonHelper = new JsonHelper();
        rootNode = jsonHelper.getObjectMapper().readTree(jsonString);
    }

    /**
     * Gets a node via JSON pointer from the root JsonNode and unmarshals it to the specified class
     * @param ref The json pointer
     * @param clazz The class to unmarshal to
     * @param <T>
     * @return The object or null (if not found)
     */
    public <T> T getObjectFromRef(String ref, Class<T> clazz)
    {
        return jsonHelper.getObjectFromRef(rootNode, ref, clazz);
    }

    /**
     * Gets the root node and unmarshals it to the specified class
     * @param clazz The class to unmarshal to
     * @param <T>
     * @return The object or null (if not found)
     */
    public <T> T getObject(Class<T> clazz)
    {
        return jsonHelper.getObjectFromRef(rootNode, "", clazz);
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public JsonNode getRootNode()
    {
        return rootNode;
    }
}
