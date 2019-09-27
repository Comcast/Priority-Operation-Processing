package com.theplatform.dfh.cp.endpoint.agendatemplate.map.parameters;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public abstract class ParametersExtractor
{
    public abstract void updateParameterMap(Map<String, JsonNode> parameterMap, JsonNode sourceParameters);

    public void putParameter(Map<String, JsonNode> parameterMap, String key, JsonNode value)
    {
        if(parameterMap.containsKey(key))
        {
            // TODO: probably collect all of these... not just error on first
            throw new DuplicateParameterException(String.format("Parameter [%1$s] is duplicated.", key));
        }
        parameterMap.put(key, value);
    }
}
