package com.theplatform.dfh.cp.scheduling.api;

import java.util.Date;

/**
 *
 */
public class ReadyAgenda implements AgendaInfo
{
    private String insightId;
    private String agendaId;
    private String customerId;
    private Date added;

    public String getInsightId()
    {
        return insightId;
    }

    public void setInsightId(String insightId)
    {
        this.insightId = insightId;
    }

    public String getAgendaId()
    {
        return agendaId;
    }

    public void setAgendaId(String agendaId)
    {
        this.agendaId = agendaId;
    }

    public String getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(String customerId)
    {
        this.customerId = customerId;
    }

    public Date getAdded()
    {
        return added;
    }

    public void setAdded(Date added)
    {
        this.added = added;
    }
}
