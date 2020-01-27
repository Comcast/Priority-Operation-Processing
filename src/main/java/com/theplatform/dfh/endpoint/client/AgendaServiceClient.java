package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class AgendaServiceClient extends FissionServiceClient
{
    private static final String submitAgendaEndpoint = "submit";

    private String agendaServiceUrl;

    public AgendaServiceClient(HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        super(httpUrlConnectionFactory);
    }

    public AgendaServiceClient(HttpURLConnectionFactory httpUrlConnectionFactory, String agendaServiceUrl)
    {
        super(httpUrlConnectionFactory);
        this.agendaServiceUrl = agendaServiceUrl;
    }

    public SubmitAgendaResponse submitAgenda(SubmitAgendaRequest submitAgendaRequest)
    {
        return new GenericFissionClient<>(
            String.join("/", agendaServiceUrl, submitAgendaEndpoint), getHttpUrlConnectionFactory(), SubmitAgendaResponse.class)
            .getObjectFromPOST(submitAgendaRequest);
    }

    public AgendaServiceClient setAgendaServiceUrl(String agendaServiceUrl)
    {
        this.agendaServiceUrl = agendaServiceUrl;
        return this;
    }
}
