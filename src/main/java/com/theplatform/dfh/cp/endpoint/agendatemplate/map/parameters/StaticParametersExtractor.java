package com.theplatform.dfh.cp.endpoint.agendatemplate.map.parameters;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;

public class StaticParametersExtractor extends ParametersExtractor
{
    @Override
    public void updateParameterMap(Map<String, JsonNode> parameterMap, JsonNode templateStaticParameters)
    {
        if(parameterMap == null) return;
        Iterator<Map.Entry<String, JsonNode>> staticParametersIterator = templateStaticParameters.fields();
        while(staticParametersIterator.hasNext())
        {
            Map.Entry<String, JsonNode> entry = staticParametersIterator.next();
            // TODO: dupe check
            putParameter(parameterMap, entry.getKey(), entry.getValue());
        }
    }
}
