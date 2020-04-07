package com.cts.fission.scheduling.queue.monitor;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
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
        ObjectPersister<InsightScheduleInfo> insightSchedulingInfoPersister)
    {
         return new QueueMonitor(readyAgendaQueueFactory, readyAgendaPersister, insightClient, customerClient, insightSchedulingInfoPersister);
    }
}
