package com.comcast.pop.modules.monitor.metric;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metric;
import com.codahale.metrics.Timer;

import java.util.HashSet;
import java.util.Set;

public class MetricFilterBuilder
{
    private Set<String> filterNames = new HashSet<>();
    private boolean filterCountZero = false;
    private boolean filterTimer = false;

    public MetricFilterBuilder filterCountZero()
    {
        this.filterCountZero = true;
        return this;
    }

    public MetricFilterBuilder filterMetricName(String name)
    {
        this.filterNames.add(name);
        return this;
    }

    public MetricFilterBuilder filterTimer()
    {
        this.filterTimer = true;
        return this;
    }

    public MetricFilter build()
    {
        return new MetricFilter(filterNames, filterCountZero, filterTimer);
    }

    public static class MetricFilter implements com.codahale.metrics.MetricFilter
    {
        private Set<String> filterNames;
        private boolean filterCountZero;
        private boolean filterTimer;

        public MetricFilter(Set<String> filterNames, boolean filterCountZero, boolean filterTimer)
        {
            this.filterNames = filterNames == null ? new HashSet<>() : filterNames;
            this.filterCountZero = filterCountZero;
            this.filterTimer = filterTimer;
        }

        @Override
        public boolean matches(String name, Metric metric)
        {
            if(metric != null)
            {
                if(filterCountZero && metric instanceof Counting && ((Counting) metric).getCount() == 0)
                {
                    return false;
                }
                else if(filterTimer && metric instanceof Timer)
                {
                    return false;
                }
            }
            if(name != null && filterNames.size() != 0 && filterNames.contains(name))
            {
                return false;
            }
            return true;
        }
    }
}
