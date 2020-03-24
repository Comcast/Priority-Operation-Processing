package com.cts.fission.scheduling.queue.algorithm;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.cts.fission.scheduling.queue.InsightScheduleInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class BaseAgendaSchedulerTest
{
    protected ObjectPersister<ReadyAgenda> mockReadyAgendaPersister;
    protected Insight insight = new Insight();
    protected InsightScheduleInfo insightScheduleInfo = new InsightScheduleInfo();

    protected DataObjectFeed<ReadyAgenda> createReadyAgendaFeed(String customerId, int readyAgendaCount)
    {
        return createReadyAgendaFeed(Arrays.asList(customerId), readyAgendaCount);
    }

    protected DataObjectFeed<ReadyAgenda> createReadyAgendaFeed(String... customerIds)
    {
        return createReadyAgendaFeed(Arrays.asList(customerIds), 1);
    }

    protected DataObjectFeed<ReadyAgenda> createReadyAgendaFeed(List<String> customerIds, int readyAgendaCount)
    {
        DataObjectFeed<ReadyAgenda> readyAgendaFeed = new DataObjectFeed<>();
        customerIds.forEach(customerId ->
            {
                readyAgendaFeed.addAll(
                    IntStream.range(0, readyAgendaCount).mapToObj(i ->
                    {
                        ReadyAgenda readyAgenda = new ReadyAgenda();
                        readyAgenda.setCustomerId(customerId);
                        return readyAgenda;
                    }).collect(Collectors.toList())
                );
            });
        return readyAgendaFeed;
    }

    protected List<ReadyAgenda> createReadyAgendas(String customerId, int readyAgendaCount)
    {
        return IntStream.range(0, readyAgendaCount).mapToObj(i ->
        {
            ReadyAgenda readyAgenda = new ReadyAgenda();
            readyAgenda.setCustomerId(customerId);
            return readyAgenda;
        }).collect(Collectors.toList());
    }

    protected Customer createCustomer(String id)
    {
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }
}
