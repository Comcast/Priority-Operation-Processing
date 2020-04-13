package com.comcast.pop.endpoint.resourcepool.insight.mapper;

import com.comcast.pop.api.Agenda;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;
import java.util.TreeSet;


public abstract class AbstractInsightKeyValueMapper extends AbstractInsightMapper
{
    public AbstractInsightKeyValueMapper(String name)
    {
        super(name);
    }

    public abstract boolean matches(Agenda agenda);

    private Set<Pair<String, String>> keyValuePairs = new TreeSet<>();

    public Set<Pair<String, String>> getKeyValues()
    {
        return keyValuePairs;
    }

    @Override
    public AbstractInsightKeyValueMapper withMatchValue(String matchValue)
    {
        if(matchValue == null || matchValue.indexOf("=") <= 0) return this;
        String[] keyValue = matchValue.trim().split("=");
        String value = keyValue[1] == null ? null : keyValue[1].toLowerCase();
        keyValuePairs.add(new ImmutablePair<>(keyValue[0].toLowerCase(), value));
        super.withMatchValue(matchValue);
        return this;
    }
    public boolean matches(String key, String value)
    {
        if(key == null) return false;
        ImmutablePair<String, String> pairToMatch = new ImmutablePair<>(key.toLowerCase(), value != null ? value.toLowerCase() : null);
        return keyValuePairs.contains(pairToMatch);
    }
}
