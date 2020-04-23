package com.comcast.pop.endpoint.api.resourcepool;

import com.comcast.pop.api.Agenda;

import java.util.Collection;

public class CreateAgendaRequest
{
    private Collection<Agenda> dataObjects;

    public CreateAgendaRequest(){}

    public CreateAgendaRequest(Collection<Agenda> dataObjects)
    {
        this.dataObjects = dataObjects;
    }

    public Collection<Agenda> getAgendas()
    {
        return dataObjects;
    }

    public void setAgendas(Collection<Agenda> dataObjects)
    {
        this.dataObjects = dataObjects;
    }
}
