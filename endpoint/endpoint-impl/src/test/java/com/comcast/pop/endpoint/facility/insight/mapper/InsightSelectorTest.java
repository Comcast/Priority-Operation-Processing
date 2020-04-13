package com.comcast.pop.endpoint.facility.insight.mapper;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.facility.InsightMapper;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.endpoint.resourcepool.CustomerRequestProcessor;
import com.comcast.pop.endpoint.resourcepool.InsightRequestProcessor;
import com.comcast.pop.endpoint.resourcepool.insight.mapper.InsightSelector;
import com.comcast.pop.endpoint.resourcepool.insight.mapper.OperationNameMapper;
import com.comcast.pop.endpoint.resourcepool.insight.mapper.OperationTypeMapper;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.persistence.api.PersistenceException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InsightSelectorTest
{
    private static final InsightRequestProcessor insightRequestProcessor = mock(InsightRequestProcessor.class);
    private static final CustomerRequestProcessor customerRequestProcesssor = mock(CustomerRequestProcessor.class);
    private static final DataObjectResponse<Insight> insightResponse = mock(DataObjectResponse.class);
    private static final String INSIGHT_ID_1 = "insightId1";
    private static final String INSIGHT_ID_2 = "insightId2";
    private static final String INSIGHT_TITLE_1 = "insightTitle1";
    private static final String INSIGHT_TITLE_2 = "insightTitle2";
    private static final Insight insight1 = new Insight();
    private static final Insight insight2 = new Insight();
    private static final InsightSelector selector = new InsightSelector(insightRequestProcessor, customerRequestProcesssor);
    private static final Agenda agenda = mock(Agenda.class);
    private static final String CUSTOMER_ID_AGENDA = "MYCustId";
    private static final String INSIGHT_OPERATION_NAME_1 = "INSIGHT_OPERATION_NAME_1";
    private static final String INSIGHT_OPERATION_TYPE_1 = "INSIGHT_OPERATION_TYPE_1";
    private static final String INSIGHT_OPERATION_TYPE_2A = "INSIGHT_OPERATION_TYPE_2A";
    private static final String INSIGHT_OPERATION_TYPE_2B = "INSIGHT_OPERATION_TYPE_2B";

    @BeforeMethod
    public void setUp() throws PersistenceException
    {
        insight1.setIsGlobal(true);
        insight1.setTitle(INSIGHT_TITLE_1);
        insight1.setId(INSIGHT_ID_1);
        insight2.setIsGlobal(true);
        insight2.setTitle(INSIGHT_TITLE_2);
        insight2.setId(INSIGHT_ID_2);

        final String resourcePoolId = "myResourcPoolId76786";
        Customer agendaCustomer = new Customer();
        agendaCustomer.setCustomerId(CUSTOMER_ID_AGENDA);
        agendaCustomer.setResourcePoolId(resourcePoolId);
        DataObjectResponse<Customer> customerResponse = new DefaultDataObjectResponse<>();
        customerResponse.add(agendaCustomer);
        when(agenda.getCustomerId()).thenReturn(CUSTOMER_ID_AGENDA);
        when(insightRequestProcessor.handleGET(any())).thenReturn(insightResponse);
        when(customerRequestProcesssor.handleGET(any())).thenReturn(customerResponse);
        when(insightResponse.getAll()).thenReturn(Arrays.asList(insight1, insight2));

        OperationNameMapper nameMapper = new OperationNameMapper().withMatchValue(INSIGHT_OPERATION_NAME_1);
        OperationTypeMapper typeMapper = new OperationTypeMapper().withMatchValue(INSIGHT_OPERATION_TYPE_1);
        insight1.addMapper(nameMapper);
        insight1.addMapper(typeMapper);

        InsightMapper operationTypeMapper = new OperationTypeMapper().withMatchValue(INSIGHT_OPERATION_TYPE_2A).withMatchValue(INSIGHT_OPERATION_TYPE_2B);
        insight2.addMapper(operationTypeMapper);
    }

    @Test
    public void testNoMatch()
    {
        Operation operation = new Operation();
        operation.setName("wrong operation");
        when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Assert.assertNull(selector.select(agenda));
    }
    @Test
    public void testMatchInsight1()
    {
        Operation operation = new Operation();
        operation.setName(INSIGHT_OPERATION_NAME_1);
        when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Insight insight = selector.select(agenda);
        Assert.assertNotNull(insight);
        Assert.assertEquals(insight1, insight);

        operation = new Operation();
        operation.setType(INSIGHT_OPERATION_TYPE_1);
        when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        insight = selector.select(agenda);
        Assert.assertNotNull(insight);
        Assert.assertEquals(insight1, insight);

    }
    @Test
    public void testMatchInsight2()
    {
        Operation operation = new Operation();
        operation.setType(INSIGHT_OPERATION_TYPE_2A);
        when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Insight insight = selector.select(agenda);
        Assert.assertNotNull(insight);
        Assert.assertEquals(insight2, insight);
    }

    @DataProvider
    public Object[][] defaultIdTitleMatchProvider()
    {
        return new Object[][]
            {
                {INSIGHT_ID_2, insight2},
                {INSIGHT_TITLE_2, insight2},
                {"MissingInsightIdenitifier", insight1} // should default to first that matches
            };
    }

    @Test(dataProvider = "defaultIdTitleMatchProvider")
    public void testDefaultMatch(String defaultInsight, Insight expectedInsight)
    {
        // all insights may match
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(InsightSelector.PARAM_DEFAULT_INSIGHT, defaultInsight);
        when(agenda.getParams()).thenReturn(paramsMap);
        // NOTE: internally both insights technically match
        when(agenda.getOperations()).thenReturn(Arrays.asList(
            createOperation(INSIGHT_OPERATION_TYPE_1),
            createOperation(INSIGHT_OPERATION_TYPE_2A),
            createOperation(INSIGHT_OPERATION_TYPE_2B)));

        Insight selectedInsight = selector.select(agenda);
        Assert.assertNotNull(selectedInsight);
        Assert.assertEquals(expectedInsight, selectedInsight);
    }

    protected Operation createOperation(String type)
    {
        Operation operation = new Operation();
        operation.setType(type);
        return operation;
    }

    ////////
    //NOTE: These tests need to be performed at a different level.  insightRequestProcessor is mocked so it's all faked out anyway
    ////////
    @Test(enabled = false)
    public void testMatchInsight2ButNotInAllowedCustomers()
    {
        insight2.setIsGlobal(false);
        Operation operation = new Operation();
        operation.setType(INSIGHT_OPERATION_TYPE_2A);
        when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Insight insight = selector.select(agenda);
        Assert.assertNull(insight);
    }
    @Test(enabled = false)
    public void testMatchInsight2ButIsInAllowedCustomers()
    {
        insight2.setIsGlobal(false);
        insight2.setAllowedCustomerIds(Collections.singleton(CUSTOMER_ID_AGENDA));
        Operation operation = new Operation();
        operation.setType(INSIGHT_OPERATION_TYPE_2A);
        when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Insight insight = selector.select(agenda);
        Assert.assertNull(insight);
    }
}
