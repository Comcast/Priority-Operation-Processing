package com.comcast.pop.scheduling.monitor;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.modules.monitor.metric.MetricReporter;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.persistence.api.ObjectPersister;

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
