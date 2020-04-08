package com.comcast.pop.endpoint.client;

import com.comcast.pop.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryResponse;
import com.comcast.pop.http.api.HttpURLConnectionFactory;

public class ProgressServiceClient extends POPServiceClient
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
        return new GenericPOPClient<>(progressSummaryUrl, getHttpUrlConnectionFactory(), ProgressSummaryResponse.class)
            .getObjectFromPOST(progressSummaryRequest);
    }

    public ProgressServiceClient setProgressSummaryUrl(String progressSummaryUrl)
    {
        this.progressSummaryUrl = progressSummaryUrl;
        return this;
    }
}
