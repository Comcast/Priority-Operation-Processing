package com.theplatform.dfh.cp.modules.monitor.metric;

import com.codahale.metrics.Timer;
import com.theplatform.dfh.cp.modules.monitor.alive.AliveCheckListener;

/**
 * Simple timer metric around failed alive checks.
 */
public class MetricAliveCheckListener implements AliveCheckListener
{
    Timer timer;
    Timer.Context timerContext;

    public MetricAliveCheckListener(MetricReport reporter)
    {
        //@todo babs -- Make the metric value configurable.
        timer = reporter.getMetricRegistry().timer("AliveCheck.failed");
    }

    @Override
    public void processAliveCheck(boolean isAlive)
    {
        if (!isAlive)
        {
            if (timerContext == null)
            {
                timerContext = timer.time();
            }
        }
        else if (timerContext != null)
        {
            timerContext.stop();
            timerContext = null;
        }
    }
}
