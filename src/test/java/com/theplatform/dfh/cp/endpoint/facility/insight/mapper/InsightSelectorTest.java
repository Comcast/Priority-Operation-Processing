package com.theplatform.dfh.cp.endpoint.facility.insight.mapper;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.InsightMapper;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

public class InsightSelectorTest
{
    private static final ObjectClient<Insight> insightClient = Mockito.mock(ObjectClient.class);
    private static final ObjectClient<Customer> customerClient = Mockito.mock(ObjectClient.class);
    private static final DataObjectResponse<Insight> insightFeed = Mockito.mock(DataObjectResponse.class);
    private static final Insight insight1 = new Insight();
    private static final Insight insight2 = new Insight();
    private static final InsightSelector selector = new InsightSelector(insightClient, customerClient);
    private static final Agenda agenda = Mockito.mock(Agenda.class);
    private static final String CUSTOMER_ID = "MYCustId";
    private static final String INSIGHT_OPERATION_NAME_1 = "INSIGHT_OPERATION_NAME_1";
    private static final String INSIGHT_OPERATION_TYPE_1 = "INSIGHT_OPERATION_TYPE_1";
    private static final String INSIGHT_OPERATION_TYPE_2A = "INSIGHT_OPERATION_TYPE_2A";
    private static final String INSIGHT_OPERATION_TYPE_2B = "INSIGHT_OPERATION_TYPE_2B";

    @BeforeMethod
    public void setUp() throws Exception
    {
        final String resourcePoolId = "myResourcPoolId76786";
        DataObjectResponse<Customer> customerFeed = new DefaultDataObjectResponse<>();
        Customer customer = new Customer();
        customer.setId(CUSTOMER_ID);
        customer.setResourcePoolId(resourcePoolId);
        customerFeed.add(customer);
        Mockito.when(agenda.getCustomerId()).thenReturn(CUSTOMER_ID);
        Mockito.when(insightClient.getObjects(Mockito.anyList())).thenReturn(insightFeed);
        Mockito.when(customerClient.getObject(Mockito.anyString())).thenReturn(customerFeed);
        Mockito.when(insightFeed.getAll()).thenReturn(Arrays.asList(insight1, insight2));

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
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Assert.assertNull(selector.select(agenda));
    }
    @Test
    public void testMatchInsight1()
    {
        Operation operation = new Operation();
        operation.setName(INSIGHT_OPERATION_NAME_1);
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Insight insight = selector.select(agenda);
        Assert.assertNotNull(insight);
        Assert.assertEquals(insight1, insight);

        operation = new Operation();
        operation.setType(INSIGHT_OPERATION_TYPE_1);
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        insight = selector.select(agenda);
        Assert.assertNotNull(insight);
        Assert.assertEquals(insight1, insight);

    }
    @Test
    public void testMatchInsight2()
    {
        Operation operation = new Operation();
        operation.setType(INSIGHT_OPERATION_TYPE_2A);
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Insight insight = selector.select(agenda);
        Assert.assertNotNull(insight);
        Assert.assertEquals(insight2, insight);
    }
}
