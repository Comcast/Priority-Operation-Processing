package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.AgendaServiceClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Use the AgendaProvider API Gateway to get an Agenda from the DynamoDB in AWS
 */
public class AwsAgendaProviderClient implements AgendaClient
{
    private AgendaServiceClient agendaServiceClient;
    private static Logger logger = LoggerFactory.getLogger(AwsAgendaProviderClient.class);

    public AwsAgendaProviderClient(String agendaProviderUrl, HttpURLConnectionFactory httpURLConnectionFactory)
    {
        agendaServiceClient = new AgendaServiceClient(httpURLConnectionFactory).setAgendaProviderUrl(agendaProviderUrl);
    }

    public AwsAgendaProviderClient(AgendaServiceClient agendaServiceClient)
    {
        this.agendaServiceClient = agendaServiceClient;
    }

    public GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest)
    {
        logger.debug("AwsAgendaProviderClient: Getting agenda");
        GetAgendaResponse response = agendaServiceClient.getAgenda(getAgendaRequest);
        logger.debug("AwsAgendaProviderClient: Got response: [" + (response == null ? "null" : response.toString()) +"]");
        return response;
    }
}
