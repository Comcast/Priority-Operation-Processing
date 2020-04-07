package com.theplatform.dfh.endpoint.client;

import com.comcast.pop.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryResponse;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

public class ProgressServiceClient extends FissionServiceClient
{
    private String progressSummaryUrl;

    public ProgressServiceClient(HttpURLConnectionFactory httpUrlConnectionFactory)
    {
        super(httpUrlConnectionFactory);
    }

    public ProgressServiceClient(HttpURLConnectionFactory httpUrlConnectionFactory, String progressSummaryUrl)
    {
        super(httpUrlConnectionFactory);
        this.progressSummaryUrl = progressSummaryUrl;
    }

    public ProgressSummaryResponse getProgressSummary(ProgressSummaryRequest progressSummaryRequest)
    {
        return new GenericFissionClient<>(progressSummaryUrl, getHttpUrlConnectionFactory(), ProgressSummaryResponse.class)
            .getObjectFromPOST(progressSummaryRequest);
    }

    public ProgressServiceClient setProgressSummaryUrl(String progressSummaryUrl)
    {
        this.progressSummaryUrl = progressSummaryUrl;
        return this;
    }
}
