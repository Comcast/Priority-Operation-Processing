package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class ResourcePoolServiceClient extends FissionServiceClient
{
    private String agendaProviderUrl;

    public ResourcePoolServiceClient(HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        super(httpUrlConnectionFactory);
    }

    public ResourcePoolServiceClient(HttpURLConnectionFactory httpUrlConnectionFactory, String agendaProviderUrl)
    {
        super(httpUrlConnectionFactory);
        this.agendaProviderUrl = agendaProviderUrl;
    }

    public GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest)
    {
        return new GenericFissionClient<>(agendaProviderUrl, getHttpUrlConnectionFactory(), GetAgendaResponse.class)
            .getObjectFromPOST(getAgendaRequest);
    }

    public ResourcePoolServiceClient setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
    }
}
