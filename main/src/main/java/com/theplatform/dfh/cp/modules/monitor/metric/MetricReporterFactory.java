package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;

public interface MetricReporterFactory
{
    ScheduledReporter register(MetricRegistry registry);
    int getReportIntervalInMilli();
}
