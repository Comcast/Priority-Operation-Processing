package com.theplatform.dfh.cp.endpoint.progress.service.api;

import com.theplatform.dfh.endpoint.api.DefaultServiceRequest;

public class ProgressSummaryRequest
{
    private String linkId;

    public ProgressSummaryRequest()
    {
    }

    public ProgressSummaryRequest(String linkId)
    {
        this.linkId = linkId;
    }

    public String getLinkId()
    {
        return linkId;
    }

    public ProgressSummaryRequest setLinkId(String linkId)
    {
        this.linkId = linkId;
        return this;
    }
}
