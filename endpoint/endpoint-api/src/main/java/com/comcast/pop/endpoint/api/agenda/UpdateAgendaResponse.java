package com.comcast.pop.endpoint.api.agenda;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.endpoint.api.DefaultServiceResponse;

public class UpdateAgendaResponse extends DefaultServiceResponse
{
    private Agenda agenda;

    public UpdateAgendaResponse()
    {
    }

    public Agenda getAgenda()
    {
        return agenda;
    }

    public void setAgenda(Agenda agenda)
    {
        this.agenda = agenda;
    }
}

