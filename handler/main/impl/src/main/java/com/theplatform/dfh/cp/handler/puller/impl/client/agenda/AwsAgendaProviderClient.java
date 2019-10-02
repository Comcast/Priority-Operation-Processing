package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use the AgendaProvider API Gateway to get an Agenda from the DynamoDB in AWS
 */
public class AwsAgendaProviderClient implements AgendaClient
{
    private ResourcePoolServiceClient resourcePoolServiceClient;
    private static Logger logger = LoggerFactory.getLogger(AwsAgendaProviderClient.class);

    public AwsAgendaProviderClient(String agendaProviderUrl, HttpURLConnectionFactory httpURLConnectionFactory)
    {
        resourcePoolServiceClient = new ResourcePoolServiceClient(httpURLConnectionFactory).setAgendaProviderUrl(agendaProviderUrl);
    }

    public AwsAgendaProviderClient(ResourcePoolServiceClient ResourcePoolServiceClient)
    {
        this.resourcePoolServiceClient = ResourcePoolServiceClient;
    }

    public GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest)
    {
        logger.debug("AwsAgendaProviderClient: Getting agenda");
        GetAgendaResponse response = resourcePoolServiceClient.getAgenda(getAgendaRequest);
        logger.debug("AwsAgendaProviderClient: Got response: [" + (response == null ? "null" : response.toString()) +"]");
        return response;
    }
}
