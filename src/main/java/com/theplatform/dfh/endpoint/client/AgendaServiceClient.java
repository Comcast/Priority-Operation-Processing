package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaResponse;
import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class AgendaServiceClient extends FissionServiceClient
{
    private static final String SUBMIT_AGENDA_ENDPOINT = "submit";
    private static final String RETRY_AGENDA_ENDPOINT = "retry";

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
            String.join("/", agendaServiceUrl, SUBMIT_AGENDA_ENDPOINT), getHttpUrlConnectionFactory(), SubmitAgendaResponse.class)
            .getObjectFromPOST(submitAgendaRequest);
    }

    public RetryAgendaResponse retryAgenda(RetryAgendaRequest retryAgendaRequest)
    {
        return new GenericFissionClient<>(
            String.join("/", agendaServiceUrl, RETRY_AGENDA_ENDPOINT), getHttpUrlConnectionFactory(), RetryAgendaResponse.class)
            .getObjectFromPOST(retryAgendaRequest);
    }

    public AgendaServiceClient setAgendaServiceUrl(String agendaServiceUrl)
    {
        this.agendaServiceUrl = agendaServiceUrl;
        return this;
    }
}
