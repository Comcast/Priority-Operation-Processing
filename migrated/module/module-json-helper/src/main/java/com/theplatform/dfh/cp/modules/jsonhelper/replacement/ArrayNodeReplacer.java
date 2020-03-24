package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Performs the replacement operation for Json Array indices
 */
public class ArrayNodeReplacer implements JsonNodeReplacer
{
    private int replacementIndex;
    private ArrayNode arrayNode;

    public ArrayNodeReplacer configureArrayIndex(ArrayNode arrayNode, int replacementIndex)
    {
        this.arrayNode = arrayNode;
        this.replacementIndex = replacementIndex;
        return this;
    }

    @Override
    public void updateValue(String newValue)
    {
        arrayNode.remove(replacementIndex);
        arrayNode.insert(replacementIndex, newValue);
    }

    @Override
    public void updateValue(JsonNode newValue)
    {
        arrayNode.remove(replacementIndex);
        arrayNode.insert(replacementIndex, newValue);
    }
}
