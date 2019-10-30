package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.endpoint.api.ServiceResponse;
import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaResponse;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressResponse;
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
