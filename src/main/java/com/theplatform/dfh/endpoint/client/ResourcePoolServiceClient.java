package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaResponse;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourcePoolServiceClient extends FissionServiceClient
{
    private String agendaProviderUrl;
    private String agendaCreateUrl;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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
        logger.info("Getting agenda with connection factory [" + getHttpUrlConnectionFactory().getClass() + "]");
        return new GenericFissionClient<>(agendaProviderUrl, getHttpUrlConnectionFactory(), GetAgendaResponse.class)
            .getObjectFromPOST(getAgendaRequest);
    }

    public CreateAgendaResponse createAgenda(CreateAgendaRequest createAgendaRequest)
    {
        logger.info("Creating agenda with connection factory [" + getHttpUrlConnectionFactory().getClass() + "]");
        return new GenericFissionClient<>(agendaCreateUrl, getHttpUrlConnectionFactory(), CreateAgendaResponse.class)
            .getObjectFromPOST(createAgendaRequest);
    }

    public ResourcePoolServiceClient setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
    }

    public void setAgendaCreateUrl(String agendaCreateUrl)
    {
        this.agendaCreateUrl = agendaCreateUrl;
    }
}
