package com.theplatform.dfh.endpoint.api.agenda.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Parameters and mapping utility for the AgendaRetry
 */
public enum ReigniteAgendaParameter
{
    RESET_ALL("resetAll"), // all ops reset
    SKIP_EXECUTION("skipExecution"), // do not submit a ready agenda
    OPERATIONS_TO_RESET("operationsToReset"), // list of operations specifically to reset to WAITING
    CONTINUE("continue"); // leave states as-is and let executor try again (failed/in-progress will be re-executed)

    public static final String SEPARATOR = "=";
    public static final String VALUE_DELIMITER = ",";
    private final String parameterName;

    ReigniteAgendaParameter(String parameterName)
    {
        this.parameterName = parameterName;
        AgendaReigniteParameters.parameterMap.put(parameterName, this);
    }

    public String getParameterName()
    {
        return parameterName;
    }

    public String getParameterNameWithValue(String value)
    {
        return parameterName + SEPARATOR + value;
    }

    public String getParameterNameWithValue(Collection<String> values)
    {
        return parameterName + SEPARATOR +
            values == null
            ? ""
            : String.join(VALUE_DELIMITER, values);
    }

    private static class AgendaReigniteParameters
    {
        private static final Map<String, ReigniteAgendaParameter> parameterMap = new HashMap<>();
    }

    public static Map<ReigniteAgendaParameter, String> getParametersMap(List<String> parameters)
    {
        Map<ReigniteAgendaParameter, String> paramatersMap = new HashMap<>();
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
                    ReigniteAgendaParameter reigniteAgendaParameter = AgendaReigniteParameters.parameterMap.get(parameterName);
                    if(reigniteAgendaParameter != null)
                    {
                        paramatersMap.put(reigniteAgendaParameter, parameterValue);
                    }
                });
        }
        return paramatersMap;
    }

}
