package com.cts.fission.scheduling.monitor;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class QueueMetricMonitorFactory
{
    public QueueMetricMonitorFactory()
    {
    }

    public QueueMetricMonitor createQueueMonitor(
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectClient<Insight> insightClient,
        MetricReporter reporter)
    {
         return new QueueMetricMonitor(readyAgendaPersister, insightClient, reporter);
    }
}
