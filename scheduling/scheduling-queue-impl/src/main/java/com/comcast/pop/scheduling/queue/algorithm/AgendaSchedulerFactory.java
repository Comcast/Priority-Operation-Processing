package com.comcast.pop.scheduling.queue.algorithm;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.persistence.api.ObjectPersister;

public class AgendaSchedulerFactory
{
    public AgendaScheduler getAgendaScheduler(Insight insight, ObjectPersister<ReadyAgenda> readyAgendaPersister, ObjectClient<Customer> customerClient)
    {
        switch(insight.getSchedulingAlgorithm())
        {
            case RoundRobin:
                return new RoundRobinAgendaScheduler(readyAgendaPersister, customerClient);
            case FirstInFirstOut:
            default:
                return new FIFOAgendaScheduler(readyAgendaPersister);
        }
    }
}
