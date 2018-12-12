package com.theplatform.dfh.cp.api.facility;

import com.theplatform.dfh.cp.api.Agenda;

import java.util.Set;

public interface InsightMapper extends Comparable
{
    public String getName();
    public Set<String> getMatchValues();
    public boolean matches(Agenda agenda);
    public InsightMapper withMatchValue(String matchValue);
}
