package com.theplatform.dfh.cp.modules.alerts.metric;

import com.codahale.metrics.Timer;
import com.theplatform.dfh.cp.modules.alerts.alive.AliveCheckListener;

public class MetricAliveCheckListener implements AliveCheckListener
{
    Timer timer;

    public MetricAliveCheckListener(MetricReporter reporter)
    {
        timer = reporter.getMetricRegistry().timer("AliveCheck.timer");
    }

    @Override
    public void processAliveCheck(boolean isAlive)
    {
        timer.time();
    }
}
