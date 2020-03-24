package com.cts.fission.scheduling.queue.algorithm;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;

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
