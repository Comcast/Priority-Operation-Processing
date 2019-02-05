package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.AgendaServiceClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

/**
 * Use the AgendaProvider API Gateway to get an Agenda from the DynamoDB in AWS
 */
public class AwsAgendaProviderClient implements AgendaClient
{
    private AgendaServiceClient agendaServiceClient;

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
        
        return agendaServiceClient.getAgenda(getAgendaRequest);
    }
}
