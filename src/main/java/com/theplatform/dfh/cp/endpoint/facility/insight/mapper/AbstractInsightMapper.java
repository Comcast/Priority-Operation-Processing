package com.theplatform.dfh.cp.endpoint.facility.insight.mapper;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.InsightMapper;

import java.util.Set;
import java.util.TreeSet;

public abstract class AbstractInsightMapper implements InsightMapper
{
    private Set<String> matchValues = new TreeSet<>();
    private String name;


    public AbstractInsightMapper(String name)
    {
        this.name = name;
    }
    public abstract boolean matches(Agenda agenda);

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Set<String> getMatchValues()
    {
        return matchValues;
    }
    public AbstractInsightMapper withMatchValue(String matchValue)
    {
        if(matchValue != null)
            this.matchValues.add(matchValue.toLowerCase());
        return this;
    }
    public boolean matches(String stringToMatch)
    {
        if(stringToMatch == null) return false;

        return matchValues.contains(stringToMatch.toLowerCase());
    }

    @Override
    public int compareTo(Object obj)
    {
        AbstractInsightMapper incomingMapper = (AbstractInsightMapper) obj;

        int nameCompare = this.name.compareTo(incomingMapper.getName());

        if(nameCompare != 0) return nameCompare;

        if (matchValues == null && incomingMapper.matchValues == null) return 0;

        if (matchValues == null) return -1;
        if (incomingMapper.matchValues == null) return 1;

        return matchValues.size() > incomingMapper.matchValues.size() ? 1 : -1;
    }
}

