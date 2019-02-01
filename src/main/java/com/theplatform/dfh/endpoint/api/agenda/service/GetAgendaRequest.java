package com.theplatform.dfh.endpoint.api.agenda.service;

import com.theplatform.dfh.endpoint.api.DefaultServiceRequest;

/**
 */
public class GetAgendaRequest extends DefaultServiceRequest
{

    private String insightId;
    private Integer count;

    public GetAgendaRequest()
    {
    }

    public GetAgendaRequest(String insightId, Integer count)
    {
        this.insightId = insightId;
        this.count = count;
    }

    public String getInsightId()
    {
        return insightId;
    }

    public void setInsightId(String insightId)
    {
        this.insightId = insightId;
    }

    public Integer getCount()
    {
        return count;
    }

    public void setCount(Integer count)
    {
        this.count = count;
    }
}
