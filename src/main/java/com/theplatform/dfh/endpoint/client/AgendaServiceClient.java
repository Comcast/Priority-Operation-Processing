package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class AgendaServiceClient extends FissionServiceClient
{
    private String agendaProviderUrl;

    public AgendaServiceClient(HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        super(httpUrlConnectionFactory);
    }

    public Agenda getAgenda()
    {
        return new GenericFissionClient<>(agendaProviderUrl, getHttpUrlConnectionFactory(), Agenda.class)
            .getObjectFromPOST(null);
    }

    public GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest)
    {
        return new GenericFissionClient<>(agendaProviderUrl, getHttpUrlConnectionFactory(), GetAgendaResponse.class)
            .getObjectFromPOST(getAgendaRequest);
    }

    public AgendaServiceClient setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
    }
}
