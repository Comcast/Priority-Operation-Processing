package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryRequest;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryResponse;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FissionClient
{
    private static final Logger logger = LoggerFactory.getLogger(FissionClient.class);

    private final HttpURLConnectionFactory httpUrlConnectionFactory;
    private String agendaProviderUrl;
    private String progressSummaryUrl;
    private JsonHelper jsonHelper = new JsonHelper();
    private URLRequestPerformer urlRequestPerformer = new URLRequestPerformer();

    public FissionClient(HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        this.httpUrlConnectionFactory = httpUrlConnectionFactory;
    }

    public Agenda getAgenda()
    {
        return new GenericFissionClient<>(agendaProviderUrl, httpUrlConnectionFactory, Agenda.class)
            .getObjectFromPOST(null);
    }

    public GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest)
    {
        return new GenericFissionClient<>(agendaProviderUrl, httpUrlConnectionFactory, GetAgendaResponse.class)
            .getObjectFromPOST(getAgendaRequest);
    }

    public ProgressSummaryResponse getProgressSummary(ProgressSummaryRequest progressSummaryRequest)
    {
        return new GenericFissionClient<>(progressSummaryUrl, httpUrlConnectionFactory, ProgressSummaryResponse.class)
            .getObjectFromPOST(progressSummaryRequest);
    }

    public FissionClient setAgendaProviderUrl(String agendaProviderUrl)
    {
        this.agendaProviderUrl = agendaProviderUrl;
        return this;
    }

    public FissionClient setProgressSummaryUrl(String progressSummaryUrl)
    {
        this.progressSummaryUrl = progressSummaryUrl;
        return this;
    }

    public void setUrlRequestPerformer(URLRequestPerformer urlRequestPerformer)
    {
        this.urlRequestPerformer = urlRequestPerformer;
    }
}
