package com.comcast.pop.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * A JsonNode value replacer
 */
public interface JsonNodeReplacer
{
    void updateValue(String newValue);
    void updateValue(JsonNode newValue);
}
