package com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.facility.InsightMapper;
import com.theplatform.dfh.cp.endpoint.resourcepool.CustomerRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.endpoint.api.auth.CustomerIdAuthorizationResponse;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InsightSelector
{
    public static final String PARAM_DEFAULT_INSIGHT = "defaultInsight";

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

        String defaultInsightId = getDefaultInsightIdentifier(agenda);
        List<Insight> matchedInsights = new LinkedList<>();

        for(Insight insight : availableInsights)
        {
            Map<String, Set<String>> insightMappers = insight.getMappers();
            if(insightMappers == null || insightMappers.size() == 0) continue;
            for(Map.Entry<String, Set<String>> mapEntry : insightMappers.entrySet())
            {
                InsightMapper insightMapper = InsightMapperRegistry.getMapper(mapEntry.getKey(), mapEntry.getValue());
                if(insightMapper != null && insightMapper.matches(agenda))
                {
                    // no desired default, done
                    if(defaultInsightId == null)
                        return insight;
                    // match the desired default to title or id, done
                    if(StringUtils.equalsIgnoreCase(defaultInsightId, insight.getTitle())
                        ||  StringUtils.equalsIgnoreCase(defaultInsightId, insight.getId()))
                    {
                        return insight;
                    }
                    // backup list as we search for matches (assumes a default is specified)
                    matchedInsights.add(insight);
                }
            }
        }
        // default not found, tolerate not found and return first from the found list
        if(matchedInsights.size() > 0)
            return matchedInsights.get(0);
        return null;
    }

    private String getDefaultInsightIdentifier(Agenda agenda)
    {
        if(agenda.getParams() == null || !agenda.getParams().containsKey(PARAM_DEFAULT_INSIGHT))
            return null;
        return agenda.getParams().getString(PARAM_DEFAULT_INSIGHT);
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

