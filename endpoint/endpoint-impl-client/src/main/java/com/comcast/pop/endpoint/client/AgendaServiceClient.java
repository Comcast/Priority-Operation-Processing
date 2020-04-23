package com.comcast.pop.endpoint.client;

import com.comcast.pop.endpoint.api.agenda.RerunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaResponse;
import com.comcast.pop.endpoint.api.agenda.RunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RunAgendaResponse;
import com.comcast.pop.http.api.HttpURLConnectionFactory;

public class AgendaServiceClient extends POPServiceClient
{
    private static final String RUN_AGENDA_ENDPOINT = "run";
    private static final String RERUN_AGENDA_ENDPOINT = "rerun";

    private String agendaServiceUrl;

    public AgendaServiceClient(HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        super(httpUrlConnectionFactory);
    }

    public AgendaServiceClient(String agendaServiceUrl, HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        super(httpUrlConnectionFactory);
        this.agendaServiceUrl = agendaServiceUrl;
    }

    public RunAgendaResponse runAgenda(RunAgendaRequest igniteAgendaRequest)
    {
        return new GenericPOPClient<>(
            String.join("/", agendaServiceUrl, RUN_AGENDA_ENDPOINT), getHttpUrlConnectionFactory(), RunAgendaResponse.class)
            .getObjectFromPOST(igniteAgendaRequest);
    }

    public RerunAgendaResponse rerunAgenda(RerunAgendaRequest rerunAgendaRequest)
    {
        return new GenericPOPClient<>(
            String.join("/", agendaServiceUrl, RERUN_AGENDA_ENDPOINT), getHttpUrlConnectionFactory(), RerunAgendaResponse.class)
            .getObjectFromPOST(rerunAgendaRequest);
    }

    public AgendaServiceClient setAgendaServiceUrl(String agendaServiceUrl)
    {
        this.agendaServiceUrl = agendaServiceUrl;
        return this;
    }
}
