package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.cp.api.EndpointDataObject;

import java.util.ArrayList;
import java.util.List;

public class ResourcePool extends EndpointDataObject
{
    private String title;
    private List<String> insightIds = null;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public void setInsightIds(List<String> insightIds)
    {
        this.insightIds = insightIds;
    }

    public List<String> getInsightIds()
    {
        return insightIds;
    }

    public void addInsightId(String insightId)
    {
        if(insightIds == null)
            insightIds = new ArrayList<>();
        insightIds.add(insightId);
    }

}
