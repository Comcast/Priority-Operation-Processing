package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.web.client.HttpCPWebClient;
import com.theplatform.dfh.cp.endpoint.web.client.api.CPWebClientAPI;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

/**
 * Use the AgendaProvider API Gateway to get an Agenda from the DynamoDB in AWS
 */
public class AwsAgendaProviderClient implements AgendaClient
{
    private CPWebClientAPI webClient;
    private JsonHelper jsonHelper = new JsonHelper();

    public AwsAgendaProviderClient(String agendaProviderUrl, HttpURLConnectionFactory httpURLConnectionFactory)
    {
        webClient = new HttpCPWebClient(agendaProviderUrl, httpURLConnectionFactory);
    }

    public AwsAgendaProviderClient(CPWebClientAPI webClient)
    {
        this.webClient = webClient;
    }

    public String getAgenda()
    {
        Agenda result = webClient.getAgenda();

        return result == null ? null : jsonHelper.getJSONString(result);
    }
}
