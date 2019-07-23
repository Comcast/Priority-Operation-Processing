package com.cts.fission.scheduling.queue.monitor;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.modules.monitor.metric.MetricReporter;
import com.cts.fission.scheduling.queue.InsightScheduleInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;

public class QueueMonitorFactory
{
    public QueueMonitorFactory()
    {
    }

    public QueueMonitor createQueueMonitor(
        ItemQueueFactory<ReadyAgenda>readyAgendaQueueFactory,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectClient<Insight> insightClient,
        ObjectClient<Customer> customerClient,
        ObjectPersister<InsightScheduleInfo> insightSchedulingInfoPersister,
        MetricReporter reporter)
    {
         return new QueueMonitor(readyAgendaQueueFactory, readyAgendaPersister, insightClient, customerClient, insightSchedulingInfoPersister, reporter);
    }
}
