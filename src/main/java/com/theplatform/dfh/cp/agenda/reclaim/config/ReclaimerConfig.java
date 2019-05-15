package com.theplatform.dfh.cp.agenda.reclaim.config;

public class ReclaimerConfig
{
    private int maxRunSeconds = 60;
    private String agendaProgressEndpointURL;

    public int getMaxRunSeconds()
    {
        return maxRunSeconds;
    }

    public ReclaimerConfig setMaxRunSeconds(int maxRunSeconds)
    {
        this.maxRunSeconds = maxRunSeconds;
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

    public boolean validate()
    {
        return true;
    }
}
