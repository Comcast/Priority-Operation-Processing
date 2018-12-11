package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.web.client.HttpCPWebClient;
import com.theplatform.dfh.cp.endpoint.web.client.api.CPWebClientAPI;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

import java.util.List;

/**
 * Use the AgendaProvider API Gateway to get an Agenda from the DynamoDB in AWS
 */
public class AwsAgendaProviderClient implements AgendaClient
{
    private CPWebClientAPI webClient;

    public AwsAgendaProviderClient(String agendaProviderUrl, HttpURLConnectionFactory httpURLConnectionFactory)
    {
        webClient = new HttpCPWebClient(agendaProviderUrl, httpURLConnectionFactory);
    }

    public AwsAgendaProviderClient(CPWebClientAPI webClient)
    {
        this.webClient = webClient;
    }

    public Agenda getAgenda()
    {
        return webClient.getAgenda();
    }

    public GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest)
    {
        
        return webClient.getAgenda(getAgendaRequest);
    }
}
