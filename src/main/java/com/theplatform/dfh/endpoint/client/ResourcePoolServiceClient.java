package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.DataObjectFeedServiceResponse;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
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

    public DataObjectFeedServiceResponse<Agenda> getAgenda(GetAgendaRequest getAgendaRequest)
    {
        return new GenericFissionClient<>(agendaProviderUrl, getHttpUrlConnectionFactory(), DataObjectFeedServiceResponse.class)
            .getObjectFromPOST(getAgendaRequest);
    }

    public ResourcePoolServiceClient setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
    }
}
