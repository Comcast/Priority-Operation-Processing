package com.comcast.pop.endpoint.agenda.factory.template.parameters;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;

/**
 * Extracts and verifies the required parameters are provided
 */
public class AgendaTemplateParametersExtractor extends ParametersExtractor
{
    private JsonNode requiredParameters;

    @Override
    public void updateParameterMap(Map<String, JsonNode> parameterMap, JsonNode inputParameters)
    {
        if(requiredParameters == null) return;
        Iterator<String> paramaterFieldIterator = requiredParameters.fieldNames();
        while(paramaterFieldIterator.hasNext())
        {
            final String templateParameterName = paramaterFieldIterator.next();
            JsonNode parameterValue = inputParameters.get(templateParameterName);
            if(parameterValue == null)
            {
                throw new MissingTemplateParameterException(String.format("[%1$s] is a required parameter", templateParameterName));
            }
            putParameter(parameterMap, templateParameterName, parameterValue);
        }
    }

    public AgendaTemplateParametersExtractor setRequiredParameters(JsonNode requiredParameters)
    {
        this.requiredParameters = requiredParameters;
        return this;
    }
}
