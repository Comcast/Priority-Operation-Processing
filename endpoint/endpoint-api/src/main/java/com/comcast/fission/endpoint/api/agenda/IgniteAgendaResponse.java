package com.comcast.fission.endpoint.api.agenda;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.theplatform.dfh.cp.api.Agenda;
import com.comcast.fission.endpoint.api.DataObjectFeedServiceResponse;
import com.comcast.fission.endpoint.api.ErrorResponse;

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

