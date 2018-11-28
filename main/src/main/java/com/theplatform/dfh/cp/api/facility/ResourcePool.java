package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ResourcePool implements IdentifiedObject
{
    private String id = UUID.randomUUID().toString();
    private String title;
    private String ownerId;
    private List<Insight> insights = new ArrayList<>();

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }

    public List<Insight> getInsights()
    {
        return insights;
    }

    public void setInsights(List<Insight> insights)
    {
        if(insights == null)
            this.insights.clear();
        this.insights = insights;
        this.insights.stream().forEach(i -> i.setResourcePoolId(this.id));
    }
    public void addInsight(Insight insight)
    {
        if(this.insights == null)
            insights = new ArrayList<>();
        this.insights.add(insight);
        insight.setResourcePoolId(this.id);
    }

}
