package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

public interface MetricReporterFactory
{
    public ScheduledReporter register(MetricRegistry registry);
    public int getReportIntervalInMilli();
}
