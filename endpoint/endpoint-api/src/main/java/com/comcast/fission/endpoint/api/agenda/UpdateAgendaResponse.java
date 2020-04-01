package com.comcast.fission.endpoint.api.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.comcast.fission.endpoint.api.DefaultServiceResponse;

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

