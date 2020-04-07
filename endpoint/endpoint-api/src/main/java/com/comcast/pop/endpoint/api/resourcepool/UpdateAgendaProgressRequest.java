package com.comcast.pop.endpoint.api.resourcepool;

import com.comcast.pop.api.progress.AgendaProgress;

public class UpdateAgendaProgressRequest
{
    private AgendaProgress agendaProgress;

    public UpdateAgendaProgressRequest(AgendaProgress agendaProgress)
    {
        this.agendaProgress = agendaProgress;
    }

    public UpdateAgendaProgressRequest()
    {
    }

    public AgendaProgress getAgendaProgress()
    {
        return agendaProgress;
    }

    public void setAgendaProgress(AgendaProgress agendaProgress)
    {
        this.agendaProgress = agendaProgress;
    }
}
