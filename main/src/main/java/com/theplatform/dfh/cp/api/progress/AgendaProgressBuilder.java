package com.theplatform.dfh.cp.api.progress;

import com.theplatform.dfh.cp.api.Agenda;

import java.util.Date;

public class AgendaProgressBuilder
{
    private AgendaProgress object;

    public AgendaProgressBuilder(AgendaProgress object)
    {
        this.object = object;
    }

    public AgendaProgressBuilder()
    {
        this.object = new AgendaProgress();
        this.object.setAddedTime(new Date());
    }

    public AgendaProgressBuilder withAgendaFields(Agenda agenda)
    {
        object.setCustomerId(agenda.getCustomerId());
        object.setLinkId(agenda.getLinkId());
        object.setCid(agenda.getCid());
        object.setAgendaId(agenda.getId());
        object.setParams(agenda.getParams());
        object.setAgendaInsight(agenda.getAgendaInsight());
        object.setId(agenda.getProgressId());
        return this;
    }

    public AgendaProgressBuilder withAgendaId(String agendaId)
    {
        this.object.setAgendaId(agendaId);
        return this;
    }
    public AgendaProgressBuilder withLinkId(String value)
    {
        this.object.setLinkId(value);
        return this;
    }
    public AgendaProgressBuilder withExternalId(String value)
    {
        this.object.setExternalId(value);
        return this;
    }
    public AgendaProgressBuilder withTitle(String value)
    {
        this.object.setTitle(value);
        return this;
    }
    public AgendaProgressBuilder withProcessingState(ProcessingState value)
    {
        this.object.setProcessingState(value);
        return this;
    }
    public AgendaProgressBuilder withProcessingStateMessage(String value)
    {
        this.object.setProcessingStateMessage(value);
        return this;
    }

    public AgendaProgress build()
    {
        return this.object;
    }
}
