package com.comcast.pop.endpoint.client;

import com.comcast.pop.endpoint.api.ServiceResponse;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaResponse;
import com.comcast.pop.endpoint.api.resourcepool.CreateAgendaRequest;
import com.comcast.pop.endpoint.api.resourcepool.CreateAgendaResponse;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaRequest;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaResponse;
import com.comcast.pop.endpoint.api.resourcepool.UpdateAgendaProgressRequest;
import com.comcast.pop.endpoint.api.resourcepool.UpdateAgendaProgressResponse;
import com.comcast.pop.endpoint.client.resourcepool.ResourcePoolEndpoint;
import com.comcast.pop.endpoint.client.resourcepool.ResourcePoolServiceConfig;
import com.comcast.pop.http.api.HttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourcePoolServiceClient extends POPServiceClient
{
    private ResourcePoolServiceConfig serviceClientConfig;
    private static Logger logger = LoggerFactory.getLogger(ResourcePoolServiceClient.class);

    public ResourcePoolServiceClient(ResourcePoolServiceConfig serviceClientConfig, HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        super(httpUrlConnectionFactory);
        this.serviceClientConfig = serviceClientConfig;
    }
    public ResourcePoolServiceClient(String serviceBaseURL, HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        super(httpUrlConnectionFactory);
        this.serviceClientConfig = new ResourcePoolServiceConfig(serviceBaseURL);
    }

    public GetAgendaResponse getAgenda(GetAgendaRequest request)
    {
        return makeServiceCall(request, GetAgendaResponse.class, ResourcePoolEndpoint.getAgenda);
    }

    public CreateAgendaResponse createAgenda(CreateAgendaRequest request)
    {
        return makeServiceCall(request, CreateAgendaResponse.class, ResourcePoolEndpoint.createAgenda);
    }

    public UpdateAgendaProgressResponse updateAgendaProgress(UpdateAgendaProgressRequest request)
    {
        return makeServiceCall(request, UpdateAgendaProgressResponse.class, ResourcePoolEndpoint.updateAgendaProgress);
    }

    public UpdateAgendaResponse updateAgenda(UpdateAgendaRequest request)
    {
        return makeServiceCall(request, UpdateAgendaResponse.class, ResourcePoolEndpoint.updateAgenda);
    }

    private <Req, Res extends ServiceResponse> Res makeServiceCall(Req request, Class<Res> responseClass, ResourcePoolEndpoint resourcePoolEndpoint)
    {
        if(logger.isDebugEnabled())
            logger.debug("Calling " + resourcePoolEndpoint.name());
        Res response = new GenericPOPClient<Res, Req>(
                serviceClientConfig.getProviderUrl(resourcePoolEndpoint),
                getHttpUrlConnectionFactory(),
                responseClass)
            .getObjectFromPOST(request);
        if(logger.isDebugEnabled())
            logger.debug("Got response: [" + (response == null ? "null" : response.toString()) +"]");
        return response;
    }
}
