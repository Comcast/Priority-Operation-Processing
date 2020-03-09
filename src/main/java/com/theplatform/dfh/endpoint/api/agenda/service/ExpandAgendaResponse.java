package com.theplatform.dfh.endpoint.api.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.DefaultServiceResponse;

public class ExpandAgendaResponse extends DefaultServiceResponse
{
    private Agenda agenda;

    public ExpandAgendaResponse()
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

