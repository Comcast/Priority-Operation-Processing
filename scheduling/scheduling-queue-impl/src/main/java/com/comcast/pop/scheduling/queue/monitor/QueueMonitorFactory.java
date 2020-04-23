package com.comcast.pop.scheduling.queue.monitor;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.scheduling.queue.InsightScheduleInfo;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.modules.queue.api.ItemQueueFactory;
import com.comcast.pop.persistence.api.ObjectPersister;

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
