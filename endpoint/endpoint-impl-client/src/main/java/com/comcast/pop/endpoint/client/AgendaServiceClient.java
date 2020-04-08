package com.comcast.pop.endpoint.client;

import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.ReigniteAgendaResponse;
import com.comcast.pop.endpoint.api.agenda.IgniteAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.IgniteAgendaResponse;
import com.comcast.pop.http.api.HttpURLConnectionFactory;

public class AgendaServiceClient extends POPServiceClient
{
    private static final String IGNITE_AGENDA_ENDPOINT = "ignite";
    private static final String REIGNITE_AGENDA_ENDPOINT = "reignite";

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

    public IgniteAgendaResponse igniteAgenda(IgniteAgendaRequest igniteAgendaRequest)
    {
        return new GenericPOPClient<>(
            String.join("/", agendaServiceUrl, IGNITE_AGENDA_ENDPOINT), getHttpUrlConnectionFactory(), IgniteAgendaResponse.class)
            .getObjectFromPOST(igniteAgendaRequest);
    }

    public ReigniteAgendaResponse reigniteAgenda(ReigniteAgendaRequest reigniteAgendaRequest)
    {
        return new GenericPOPClient<>(
            String.join("/", agendaServiceUrl, REIGNITE_AGENDA_ENDPOINT), getHttpUrlConnectionFactory(), ReigniteAgendaResponse.class)
            .getObjectFromPOST(reigniteAgendaRequest);
    }

    public AgendaServiceClient setAgendaServiceUrl(String agendaServiceUrl)
    {
        this.agendaServiceUrl = agendaServiceUrl;
        return this;
    }
}
