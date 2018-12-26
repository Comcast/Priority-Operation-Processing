package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.HttpCPWebClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

/**
 * Use the AgendaProvider API Gateway to get an Agenda from the DynamoDB in AWS
 */
public class AwsAgendaProviderClient implements AgendaClient
{
    private HttpCPWebClient webClient;

    public AwsAgendaProviderClient(String agendaProviderUrl, HttpURLConnectionFactory httpURLConnectionFactory)
    {
        webClient = new HttpCPWebClient(agendaProviderUrl, httpURLConnectionFactory);
    }

    public AwsAgendaProviderClient(HttpCPWebClient webClient)
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
