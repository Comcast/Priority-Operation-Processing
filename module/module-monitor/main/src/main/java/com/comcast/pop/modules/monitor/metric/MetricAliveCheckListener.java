package com.comcast.pop.modules.monitor.metric;

import com.codahale.metrics.Meter;
import com.comcast.pop.modules.monitor.alive.AliveCheckListener;

/**
 * Simple counter around failed alive checks.
 */
public class MetricAliveCheckListener implements AliveCheckListener
{
   Meter failedMeter;

    public MetricAliveCheckListener(MetricReporter reporter)
    {
        //@todo -- Make the metric value configurable.
        failedMeter = reporter.getMetricRegistry().meter("AliveCheck.failed");
    }

    @Override
    public void processAliveCheck(boolean isAlive)
    {
        if (!isAlive)
        {
            if (failedMeter == null)
            {
                failedMeter.mark();
            }
        }
    }
}
