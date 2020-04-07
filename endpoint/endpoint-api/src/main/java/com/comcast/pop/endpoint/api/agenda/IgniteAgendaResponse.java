package com.comcast.pop.endpoint.api.agenda;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.endpoint.api.DataObjectFeedServiceResponse;
import com.comcast.pop.endpoint.api.ErrorResponse;

import java.util.Collection;

public class IgniteAgendaResponse extends DataObjectFeedServiceResponse<Agenda>
{
    public IgniteAgendaResponse()
    {
    }

    public IgniteAgendaResponse(ErrorResponse errorResponse)
    {
        super(errorResponse);
    }

    public IgniteAgendaResponse(Collection<Agenda> dataObjects)
    {
        super(dataObjects);
    }

    public void setAgendas(Collection<Agenda> dataObjects)
    {
        super.setAll(dataObjects);
    }

    public Collection<Agenda> getAgendas()
    {
        return super.getAll();
    }

    @JsonIgnore
    public Agenda getFirst()
    {
        return super.getAll().iterator().next();
    }
}

