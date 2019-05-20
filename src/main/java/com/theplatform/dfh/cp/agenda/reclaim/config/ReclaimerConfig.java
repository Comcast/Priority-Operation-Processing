package com.theplatform.dfh.cp.agenda.reclaim.config;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class ReclaimerConfig
{
    private int maximumExecutionSeconds = 60;
    private boolean logReclaimOnly = false;
    private String agendaProgressEndpointURL;

    public int getMaximumExecutionSeconds()
    {
        return maximumExecutionSeconds;
    }

    public ReclaimerConfig setMaximumExecutionSeconds(int maximumExecutionSeconds)
    {
        this.maximumExecutionSeconds = maximumExecutionSeconds;
        return this;
    }

    public String getAgendaProgressEndpointURL()
    {
        return agendaProgressEndpointURL;
    }

    public ReclaimerConfig setAgendaProgressEndpointURL(String agendaProgressEndpointURL)
    {
        this.agendaProgressEndpointURL = agendaProgressEndpointURL;
        return this;
    }

    public boolean getLogReclaimOnly()
    {
        return logReclaimOnly;
    }

    public ReclaimerConfig setLogReclaimOnly(boolean logReclaimOnly)
    {
        this.logReclaimOnly = logReclaimOnly;
        return this;
    }

    public String validate()
    {
        List<String> validationIssues = new LinkedList<>();

        if(StringUtils.isBlank(agendaProgressEndpointURL))
            validationIssues.add("agendaProgressEndpointURL must be assigned");

        if(maximumExecutionSeconds < 0)
            validationIssues.add("maximumExecutionSeconds must be non-negative");

        return validationIssues.size() == 0
               ? null
               : String.join(",", validationIssues);

    }
}
