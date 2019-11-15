package com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.InsightMapper;
import com.theplatform.dfh.cp.endpoint.resourcepool.CustomerRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.auth.CustomerIdAuthorizationResponse;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.theplatform.dfh.persistence.api.ObjectPersister;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InsightSelector
{
    private InsightRequestProcessor insightRequestProcessor;
    private CustomerRequestProcessor customerRequestProcessor;

    public InsightSelector(ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        this(new InsightRequestProcessor(insightPersister), new CustomerRequestProcessor(customerPersister));
    }
    public InsightSelector(InsightRequestProcessor insightRequestProcessor, CustomerRequestProcessor customerRequestProcessor)
    {
        this.insightRequestProcessor = insightRequestProcessor;
        this.customerRequestProcessor = customerRequestProcessor;
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
        DataObjectRequest<Insight> insightReq = generateInsightReq(customerId, resourcePoolId);
        DataObjectResponse<Insight> insightFeed = insightRequestProcessor.handleGET(insightReq);
        if(insightFeed == null || insightFeed.getAll() == null) return null;
        return insightFeed.getAll();
    }

    private Customer lookupCustomer(final String customerId)
    {
        if(customerId == null) return null;
        DataObjectRequest<Customer> customerReq = generateCustomerReq(customerId);
        DataObjectResponse<Customer> customerResponse = customerRequestProcessor.handleGET(customerReq);
        return customerResponse.getFirst();
    }

    private DataObjectRequest<Customer> generateCustomerReq(String customerId)
    {
        DefaultDataObjectRequest<Customer> req = new DefaultDataObjectRequest<>();
        req.setAuthorizationResponse(new CustomerIdAuthorizationResponse(customerId));
        req.setId(customerId);
        return req;
    }
    private DataObjectRequest<Insight> generateInsightReq(String customerId, String resourcePoolId)
    {
        DefaultDataObjectRequest<Insight> req = new DefaultDataObjectRequest<>();
        req.setAuthorizationResponse(new CustomerIdAuthorizationResponse(customerId));
        req.setQueries(Collections.singletonList(new ByResourcePoolId(resourcePoolId)));
        return req;
    }
}

