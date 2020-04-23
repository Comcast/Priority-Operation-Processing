package com.comcast.pop.endpoint.agenda.factory.template.parameters;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;

/**
 * Extracts the static parameters from the specified JsonNode
 */
public class BasicParametersExtractor extends ParametersExtractor
{
    @Override
    public void updateParameterMap(Map<String, JsonNode> parameterMap, JsonNode templateStaticParameters)
    {
        if(parameterMap == null) return;
        Iterator<Map.Entry<String, JsonNode>> staticParametersIterator = templateStaticParameters.fields();
        while(staticParametersIterator.hasNext())
        {
            Map.Entry<String, JsonNode> entry = staticParametersIterator.next();
            putParameter(parameterMap, entry.getKey(), entry.getValue());
        }
    }
}
