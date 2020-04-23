package com.comcast.pop.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Performs the replacement operation for Json Objects
 */
public class ObjectNodeReplacer implements JsonNodeReplacer
{
    private JsonNode parentNode;
    private String field;

    public ObjectNodeReplacer configureField(JsonNode parentNode, String field)
    {
        this.parentNode = parentNode;
        this.field = field;
        return this;
    }

    @Override
    public void updateValue(String newValue)
    {
        ((ObjectNode)parentNode).put(field, newValue);
    }

    @Override
    public void updateValue(JsonNode newValue)
    {
        ((ObjectNode)parentNode).set(field, newValue);
    }
}
