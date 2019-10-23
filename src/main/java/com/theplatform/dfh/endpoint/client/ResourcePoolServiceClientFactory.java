package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class ResourcePoolServiceClientFactory
{
    private String agendaCreateUrl;
    private String agendaProviderUrl;

    public ResourcePoolServiceClient create(HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        return new ResourcePoolServiceClient(httpUrlConnectionFactory)
            .setAgendaCreateUrl(agendaCreateUrl)
            .setAgendaProviderUrl(agendaProviderUrl);
    }

    public ResourcePoolServiceClientFactory setAgendaCreateUrl(String agendaCreateUrl)
    {
        this.agendaCreateUrl = agendaCreateUrl;
        return this;
    }

    public ResourcePoolServiceClientFactory setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
    }
}
