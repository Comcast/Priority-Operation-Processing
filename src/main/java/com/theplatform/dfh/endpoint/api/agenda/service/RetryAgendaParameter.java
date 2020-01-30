package com.theplatform.dfh.endpoint.api.agenda.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters and mapping utility for the AgendaRetry
 */
public enum RetryAgendaParameter
{
    RESET_ALL("resetall");

    private static final String SEPARATOR = "=";
    private final String parameterName;

    RetryAgendaParameter(String parameterName)
    {
        this.parameterName = parameterName;
        AgendaRetryParameters.parameterMap.put(parameterName, this);
    }

    public String getParameterName()
    {
        return parameterName;
    }

    public String getParameterNameWithValue(String value)
    {
        return parameterName + SEPARATOR + value;
    }

    private static class AgendaRetryParameters
    {
        private static final Map<String, RetryAgendaParameter> parameterMap = new HashMap<>();
    }

    public static Map<RetryAgendaParameter, String> getParametersMap(List<String> parameters)
    {
        Map<RetryAgendaParameter, String> paramatersMap = new HashMap<>();
        if(parameters != null)
        {
            parameters.stream()
                .filter(Objects::nonNull)
                .filter(s -> !s.isEmpty())
                .forEach(param ->
                {
                    String[] split = param.split(SEPARATOR, 2);
                    String parameterName = split[0];
                    String parameterValue = null;
                    if(split.length > 1)
                    {
                        parameterValue = split[1];
                    }
                    RetryAgendaParameter retryAgendaParameter = AgendaRetryParameters.parameterMap.get(parameterName);
                    if(retryAgendaParameter != null)
                    {
                        paramatersMap.put(retryAgendaParameter, parameterValue);
                    }
                });
        }
        return paramatersMap;
    }

}
