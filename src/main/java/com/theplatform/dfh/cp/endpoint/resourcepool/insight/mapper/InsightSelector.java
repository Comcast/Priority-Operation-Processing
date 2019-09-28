package com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.InsightMapper;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InsightSelector
{
    private ObjectClient<Insight> insightClient;
    private ObjectClient<Customer> customerClient;

    public InsightSelector(
            HttpURLConnectionFactory httpURLConnectionFactory, String insightURL, String customerURL)
    {
        this.insightClient =
                new HttpObjectClient<>(insightURL, httpURLConnectionFactory, Insight.class);
        this.customerClient =
                new HttpObjectClient<>(customerURL, httpURLConnectionFactory, Customer.class);
    }

    public InsightSelector(
            ObjectClient<Insight> insightClient,
            ObjectClient<Customer> customerClient)
    {
        this.insightClient = insightClient;
        this.customerClient = customerClient;
    }

    public Insight select(Agenda agenda)
    {
        // scan the agenda for certain details and return the insight
        Customer customer = lookupCustomer(agenda.getCustomerId());
        if(customer == null)
            throw new ValidationException(String.format("Customer not found for customerId %s", agenda.getCustomerId()));

        List<Insight> availableInsights = lookupInsights(customer.getResourcePoolId(), customer.getId());
        if(availableInsights == null) return null;

        for(Insight insight : availableInsights)
        {
            Map<String, Set<String>> insightMappers = insight.getMappers();
            if(insightMappers == null || insightMappers.size() == 0) continue;
            for(Map.Entry<String, Set<String>> mapEntry : insightMappers.entrySet())
            {
                InsightMapper insightMapper = InsightMapperRegistry.getMapper(mapEntry.getKey(), mapEntry.getValue());
                if(insightMapper != null && insightMapper.matches(agenda)) return insight;
            }
        }
        return null;
    }
    private List<Insight> lookupInsights(final String resourcePoolId, final String customerId)
    {
        if(resourcePoolId == null) return null;

        ByResourcePoolId byResourcePoolId = new ByResourcePoolId(resourcePoolId);
        DataObjectResponse<Insight> insightFeed = insightClient.getObjects(Collections.singletonList(byResourcePoolId));
        if(insightFeed == null || insightFeed.getAll() == null) return null;
        List<Insight> allInsights = insightFeed.getAll();
        return allInsights.stream().filter(i -> i.isVisible(customerId)).collect(Collectors.toList());
    }

    private Customer lookupCustomer(final String customerId)
    {
        if(customerId == null) return null;
        DataObjectResponse<Customer> customerResponse = customerClient.getObject(customerId);
        return customerResponse.getFirst();
    }

}

