package com.theplatform.dfh.endpoint.api.agenda.service;

public class IgniteAgendaRequest
{
    private String payload;
    private String agendaTemplateId;

    public IgniteAgendaRequest()
    {
    }

    public IgniteAgendaRequest(String payload, String agendaTemplateId)
    {
        this.payload = payload;
        this.agendaTemplateId = agendaTemplateId;
    }

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }

    public String getAgendaTemplateId()
    {
        return agendaTemplateId;
    }

    public void setAgendaTemplateId(String agendaTemplateId)
    {
        this.agendaTemplateId = agendaTemplateId;
    }
}
