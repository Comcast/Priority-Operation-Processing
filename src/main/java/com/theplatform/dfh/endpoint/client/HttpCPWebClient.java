package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;

public class HttpCPWebClient
{
    private static final Logger logger = LoggerFactory.getLogger(HttpCPWebClient.class);

    private final String agendaProviderUrl;
    private final HttpURLConnectionFactory httpUrlConnectionFactory;
    private JsonHelper jsonHelper = new JsonHelper();
    private URLRequestPerformer urlRequestPerformer = new URLRequestPerformer();

    public HttpCPWebClient(String agendaProviderUrl, HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        this.httpUrlConnectionFactory = httpUrlConnectionFactory;
    }

    public Agenda getAgenda()
    {
        try
        {
            HttpURLConnection urlConnection = httpUrlConnectionFactory.getHttpURLConnection(agendaProviderUrl);
            urlConnection.setRequestMethod("POST");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                null);
            if (result == null || result.length() == 0)
            {
                return null;
            }
            return jsonHelper.getObjectFromString(result, Agenda.class);
        }
        catch(IOException e)
        {
            throw new CPWebClientException("Failed to get agenda.", e);
        }
    }

    public GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest)
    {
        try
        {
            byte[] postData = jsonHelper.getJSONString(getAgendaRequest).getBytes();
            HttpURLConnection urlConnection = httpUrlConnectionFactory.getHttpURLConnection(agendaProviderUrl);
            urlConnection.setRequestMethod("POST");
            String result = urlRequestPerformer.performURLRequest(
                urlConnection,
                postData);
            if (result == null || result.length() == 0)
            {
                return null;
            }
            return jsonHelper.getObjectFromString(result, GetAgendaResponse.class);
        }
        catch(IOException e)
        {
            throw new CPWebClientException("Failed to get agenda.", e);
        }
    }

    public void setUrlRequestPerformer(URLRequestPerformer urlRequestPerformer)
    {
        this.urlRequestPerformer = urlRequestPerformer;
    }
}
