package com.theplatform.dfh.endpoint.client;

import com.comcast.fission.endpoint.api.ServiceResponse;
import com.comcast.fission.endpoint.api.agenda.UpdateAgendaRequest;
import com.comcast.fission.endpoint.api.agenda.UpdateAgendaResponse;
import com.comcast.fission.endpoint.api.resourcepool.CreateAgendaRequest;
import com.comcast.fission.endpoint.api.resourcepool.CreateAgendaResponse;
import com.comcast.fission.endpoint.api.resourcepool.GetAgendaRequest;
import com.comcast.fission.endpoint.api.resourcepool.GetAgendaResponse;
import com.comcast.fission.endpoint.api.resourcepool.UpdateAgendaProgressRequest;
import com.comcast.fission.endpoint.api.resourcepool.UpdateAgendaProgressResponse;
import com.theplatform.dfh.endpoint.client.resourcepool.ResourcePoolEndpoint;
import com.theplatform.dfh.endpoint.client.resourcepool.ResourcePoolServiceConfig;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourcePoolServiceClient extends FissionServiceClient
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
        Res response = new GenericFissionClient<Res, Req>(
                serviceClientConfig.getProviderUrl(resourcePoolEndpoint),
                getHttpUrlConnectionFactory(),
                responseClass)
            .getObjectFromPOST(request);
        if(logger.isDebugEnabled())
            logger.debug("Got response: [" + (response == null ? "null" : response.toString()) +"]");
        return response;
    }
}
