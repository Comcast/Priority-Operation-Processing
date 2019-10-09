package com.theplatform.dfh.cp.endpoint.facility.insight.mapper;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.InsightMapper;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.OperationNameMapper;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.OperationTypeMapper;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

public class InsightSelectorTest
{
    private static final ObjectPersister<Insight> insightClient = Mockito.mock(ObjectPersister.class);
    private static final ObjectPersister<Customer> customerClient = Mockito.mock(ObjectPersister.class);
    private static final DataObjectFeed<Insight> insightFeed = Mockito.mock(DataObjectFeed.class);
    private static final Insight insight1 = new Insight();
    private static final Insight insight2 = new Insight();
    private static final InsightSelector selector = new InsightSelector(insightClient, customerClient);
    private static final Agenda agenda = Mockito.mock(Agenda.class);
    private static final String CUSTOMER_ID_AGENDA = "MYCustId";
    private static final String INSIGHT_OPERATION_NAME_1 = "INSIGHT_OPERATION_NAME_1";
    private static final String INSIGHT_OPERATION_TYPE_1 = "INSIGHT_OPERATION_TYPE_1";
    private static final String INSIGHT_OPERATION_TYPE_2A = "INSIGHT_OPERATION_TYPE_2A";
    private static final String INSIGHT_OPERATION_TYPE_2B = "INSIGHT_OPERATION_TYPE_2B";

    @BeforeMethod
    public void setUp() throws PersistenceException
    {
        insight1.setIsGlobal(true);
        insight2.setIsGlobal(true);
        final String resourcePoolId = "myResourcPoolId76786";
        Customer agendaCustomer = new Customer();
        agendaCustomer.setCustomerId(CUSTOMER_ID_AGENDA);
        agendaCustomer.setResourcePoolId(resourcePoolId);
        Mockito.when(agenda.getCustomerId()).thenReturn(CUSTOMER_ID_AGENDA);
        Mockito.when(insightClient.retrieve(Mockito.anyList())).thenReturn(insightFeed);
        Mockito.when(customerClient.retrieve(Mockito.anyString())).thenReturn(agendaCustomer);
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
    @Test
    public void testMatchInsight2ButNotInAllowedCustomers()
    {
        insight2.setIsGlobal(false);
        Operation operation = new Operation();
        operation.setType(INSIGHT_OPERATION_TYPE_2A);
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Insight insight = selector.select(agenda);
        Assert.assertNull(insight);
    }
    @Test
    public void testMatchInsight2ButIsInAllowedCustomers()
    {
        insight2.setIsGlobal(false);
        insight2.setAllowedCustomerIds(Collections.singleton(CUSTOMER_ID_AGENDA));
        Operation operation = new Operation();
        operation.setType(INSIGHT_OPERATION_TYPE_2A);
        Mockito.when(agenda.getOperations()).thenReturn(Collections.singletonList(operation));
        Insight insight = selector.select(agenda);
        Assert.assertNull(insight);
    }
}
