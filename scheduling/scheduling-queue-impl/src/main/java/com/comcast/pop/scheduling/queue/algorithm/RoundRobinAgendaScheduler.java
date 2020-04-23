package com.comcast.pop.scheduling.queue.algorithm;

import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.scheduling.queue.InsightScheduleInfo;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.comcast.pop.endpoint.api.data.query.scheduling.ByInsightIdCustomerId;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoundRobinAgendaScheduler implements AgendaScheduler
{
    private final static Logger logger = LoggerFactory.getLogger(RoundRobinAgendaScheduler.class);

    private final ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private final ObjectClient<Customer> customerClient;

    public RoundRobinAgendaScheduler(ObjectPersister<ReadyAgenda> readyAgendaPersister, ObjectClient<Customer> customerClient)
    {
        this.readyAgendaPersister = readyAgendaPersister;
        this.customerClient = customerClient;
    }

    @Override
    public List<ReadyAgenda> schedule(final int requestCount, Insight insight, InsightScheduleInfo insightScheduleInfo)
    {
        if(requestCount == 0) return null;

        PendingCustomersDecorator pendingCustomersDecorator = getPendingCustomerIds(insightScheduleInfo, insight);
        List<String> pendingCustomerIds = pendingCustomersDecorator.getPendingCustomerIds();
        if(pendingCustomerIds == null || pendingCustomerIds.size() == 0)
        {
            logger.info("No pending customers for insightId: {} Exiting.", insight.getId());
            return null;
        }

        // These are kept separate because a customer in pass one should not be removed from the pool for future queue
        List<ReadyAgenda> passOneScheduledReadyAgendas = new LinkedList<>();

        Map<String, List<ReadyAgenda>> customerReadyAgendaMap = new HashMap<>();
        List<ReadyAgenda> passTwoScheduledReadyAgendas = new LinkedList<>();

        // PASS 1: perform round robin with the remaining customer ids
        performSchedulingPass(pendingCustomerIds, insight, passOneScheduledReadyAgendas, customerReadyAgendaMap, requestCount);
        if(passOneScheduledReadyAgendas.size() >= requestCount)
        {
            // remove any customer that had work scheduled
            pendingCustomerIds.removeAll(
                passOneScheduledReadyAgendas.stream().map(ReadyAgenda::getCustomerId).collect(Collectors.toList())
            );
        }
        else
        {
            final int passTwoRequestCount = requestCount - passOneScheduledReadyAgendas.size();
            // PASS 2: Use all customers to perform round robin (new pool)
            List<String> allCustomerIds = pendingCustomersDecorator.isFullSet()
                                          ? pendingCustomersDecorator.getPendingCustomerIds()
                                          : getAllCustomersForResourcePool(insight.getResourcePoolId());

            // loop through all the customers until the request count is met or no items remain
            while(true)
            {
                int beforeRoundSize = passTwoScheduledReadyAgendas.size();
                performSchedulingPass(allCustomerIds, insight, passTwoScheduledReadyAgendas, customerReadyAgendaMap, passTwoRequestCount);
                // repeating the process did not yield any more items OR requested count was met
                if(passTwoScheduledReadyAgendas.size() >= passTwoRequestCount ||
                    beforeRoundSize == passTwoScheduledReadyAgendas.size()) break;
            }

            // remove any customers scheduled from the second pass from the entire pool
            pendingCustomerIds = allCustomerIds;
            pendingCustomerIds.removeAll(
                passTwoScheduledReadyAgendas.stream().map(ReadyAgenda::getCustomerId).collect(Collectors.toList())
            );
        }

        List<ReadyAgenda> scheduledReadyAgendas = Stream.concat(passOneScheduledReadyAgendas.stream(), passTwoScheduledReadyAgendas.stream())
            .collect(Collectors.toList());

        // set the new pending list
        insightScheduleInfo.setPendingCustomerIds(pendingCustomerIds);

        logger.info("RoundRobin Result -- InsightId: {} Scheduled: {} Requested: {} PendingCustomerIds: {}",
            insight.getId(),
            scheduledReadyAgendas.size(),
            requestCount,
            insightScheduleInfo.getPendingCustomerIds() == null ? null : insightScheduleInfo.getPendingCustomerIds().size());

        return scheduledReadyAgendas;
    }

    private void performSchedulingPass(List<String> customerIds, Insight insight, List<ReadyAgenda> scheduledReadyAgendas,
        Map<String, List<ReadyAgenda>> customerReadyAgendaMap, int requestCount)
    {
        for(String customerId : customerIds)
        {
            try
            {
                List<ReadyAgenda> customerReadyAgendas = retrieveCustomerReadyAgendas(customerId, insight, customerReadyAgendaMap);
                if(customerReadyAgendas.size() > 0)
                {
                    scheduledReadyAgendas.add(customerReadyAgendas.remove(0));
                    if(scheduledReadyAgendas.size() >= requestCount)
                    {
                        // EXIT THE LOOP -- REQUESTED COUNT MET
                        return;
                    }
                }
            }
            catch(PersistenceException e)
            {
                logger.warn(String.format("Error retrieving ReadyAgenda for customerId: %1$s. Continuing.", customerId), e);
                // despite the error just continue with the next customer
            }
        }
    }

    /**
     * Retrieves the ReadyAgendas owned by the customerId
     * @param customerId The customerId to query by
     * @param insight The insight to query by
     * @param customerReadyAgendaMap Map to retrieve and store the ReadyAgendas owned by a customer
     * @return A list of ReadyAgendas
     * @throws PersistenceException
     */
    private List<ReadyAgenda> retrieveCustomerReadyAgendas(String customerId, Insight insight, Map<String, List<ReadyAgenda>> customerReadyAgendaMap) throws PersistenceException
    {
        if(customerReadyAgendaMap.containsKey(customerId))
        {
            return customerReadyAgendaMap.get(customerId);
        }

        List<ReadyAgenda> readyAgendas;

        // TODO: limiting this to the requestCount would be ideal
        // TODO: wish list - query all items by the insight with a limit to [requestCount] per customer id (and some overall limit if possible)
        // The index should be using the 'added' field as the sort key resulting in responses being from oldest to newest
        DataObjectFeed<ReadyAgenda> readyAgendaFeed = readyAgendaPersister.retrieve(
            Collections.singletonList(new ByInsightIdCustomerId(insight.getId(), customerId)));
        if(readyAgendaFeed.isError())
        {
            logger.warn(
                String.format("There was an issue retrieving items with this customerId: %1$s for insight: %2$s", customerId, insight.getId()),
                readyAgendaFeed.getException());
            readyAgendas = new ArrayList<>();
        }
        else
        {
            readyAgendas = readyAgendaFeed.getAll();
        }
        customerReadyAgendaMap.put(customerId, readyAgendas);
        return readyAgendas;
    }

    /**
     * Gets the pending customers for the insight
     * @param insightScheduleInfo The queue info (may or may not contain the pending customers)
     * @param insight The insight to query with if necessary
     * @return
     */
    protected PendingCustomersDecorator getPendingCustomerIds(InsightScheduleInfo insightScheduleInfo, Insight insight)
    {
        List<String> pendingCustomerIds;
        boolean fullSet = false;
        if(insightScheduleInfo.getPendingCustomerIds() == null || insightScheduleInfo.getPendingCustomerIds().size() == 0)
        {
            logger.info("No pending customers, attempting to generate a new set.");
            pendingCustomerIds = getAllCustomersForResourcePool(insight.getResourcePoolId());
            fullSet = true;
        }
        else
        {
            // make a copy of the list
            pendingCustomerIds = new ArrayList<>(insightScheduleInfo.getPendingCustomerIds());
        }

        logger.info("PendingCustomerIds size: {}", pendingCustomerIds == null ? 0 : pendingCustomerIds.size());

        return new PendingCustomersDecorator(pendingCustomerIds, fullSet);
    }

    protected List<String> getAllCustomersForResourcePool(String resourcePoolId)
    {
        // TODO: this should be using a byResourcePoolId
        DataObjectResponse<Customer> allCustomers = customerClient.getObjects(Collections.singletonList(new ByResourcePoolId(resourcePoolId)));
        if(allCustomers.isError())
        {
            logger.error("Failed to generate new customer set. {}",
                allCustomers.getErrorResponse() == null ? "" : allCustomers.getErrorResponse().toString());
            // TODO: just break and let it reprocess later?
            return null;
        }
        if(allCustomers.getAll() == null)
        {
            logger.warn("Returned customer set is null.");
            return null;
        }
        return allCustomers.getAll().stream().map(Customer::getId).collect(Collectors.toList());
    }
}
