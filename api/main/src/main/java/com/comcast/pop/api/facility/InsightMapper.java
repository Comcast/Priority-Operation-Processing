package com.comcast.pop.api.facility;

import com.comcast.pop.api.Agenda;

import java.util.Set;

public interface InsightMapper extends Comparable
{
    public String getName();
    public Set<String> getMatchValues();
    public boolean matches(Agenda agenda);
    public InsightMapper withMatchValue(String matchValue);
}
