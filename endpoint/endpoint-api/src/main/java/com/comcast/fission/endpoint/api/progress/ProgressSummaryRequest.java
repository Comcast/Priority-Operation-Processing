package com.comcast.fission.endpoint.api.progress;

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