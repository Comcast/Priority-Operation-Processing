package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.web.client.HttpCPWebClient;
import com.theplatform.dfh.cp.endpoint.web.client.api.CPWebClientAPI;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.schedule.http.api.HttpURLConnectionFactory;

import javax.annotation.Resource;

/**
 * User: ktodd200
 * Date: 9/12/18
 */
public class AwsAgendaProviderClient implements AgendaClient
{
    private CPWebClientAPI webClient;
    private JsonHelper jsonHelper = new JsonHelper();

    public AwsAgendaProviderClient(String agendaProviderUrl, HttpURLConnectionFactory httpURLConnectionFactory)
    {
        webClient = new HttpCPWebClient(agendaProviderUrl, httpURLConnectionFactory);
    }

    public String getAgenda()
    {
        return jsonHelper.getJSONString(webClient.getAgenda());
    }
}
