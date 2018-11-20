package com.theplatform.dfh.cp.resourcepool.api;

import com.theplatform.dfh.cp.api.IdentifiedObject;

import java.util.ArrayList;
import java.util.List;

public class ResourcePool implements IdentifiedObject
{
    private String id;
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

    public ResourcePool setTitle(String title)
    {
        this.title = title;
        return this;
    }

    public String getOwnerId()
    {
        return ownerId;
    }

    public ResourcePool setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
        return this;
    }

    public List<Insight> getInsights()
    {
        return insights;
    }

    public ResourcePool setInsights(List<Insight> insights)
    {
        if(insights == null)
            this.insights.clear();
        this.insights = insights;
        this.insights.stream().map(i -> i.setResourcePool(this));
        return this;
    }
    public ResourcePool addInsight(Insight insight)
    {
        if(this.insights == null)
            insights = new ArrayList<>();
        this.insights.add(insight);
        insight.setResourcePool(this);
        return this;
    }

}
