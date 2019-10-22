package com.cts.fission.scheduling.queue.algorithm;

import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.cts.fission.scheduling.queue.InsightScheduleInfo;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.query.Query;
import org.apache.commons.lang3.StringUtils;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RoundRobinAgendaSchedulerTest extends BaseAgendaSchedulerTest
{
    private RoundRobinAgendaScheduler scheduler;
    private ObjectClient<Customer> mockCustomerClient;

    @BeforeMethod
    public void setup()
    {
        insight = new Insight();
        insight.setId(UUID.randomUUID().toString());
        insight.setResourcePoolId(UUID.randomUUID().toString());

        insightScheduleInfo = new InsightScheduleInfo();
        mockReadyAgendaPersister = mock(ObjectPersister.class);
        mockCustomerClient = mock(ObjectClient.class);
        scheduler = new RoundRobinAgendaScheduler(mockReadyAgendaPersister, mockCustomerClient);
    }

    @Test
    public void testGetPendingCustomerIdsNoPendingCustomers() throws Throwable
    {
        // also no customers
        DataObjectResponse<Customer> customerFeed = new DefaultDataObjectResponse<>();
        doReturn(customerFeed).when(mockCustomerClient).getObjects(anyList());
        List<String> result = scheduler.getPendingCustomerIds(insightScheduleInfo, insight).getPendingCustomerIds();
        Assert.assertEquals(result.size(), 0);
        verify(mockCustomerClient, times(1)).getObjects(anyList());
    }

    @Test
    public void testGetPendingCustomerIdsClientError() throws Throwable
    {
        DataObjectResponse<Customer> customerFeed = new DefaultDataObjectResponse<>();
        customerFeed.setErrorResponse(new ErrorResponse());
        doReturn(customerFeed).when(mockCustomerClient).getObjects(anyList());
        List<String> result = scheduler.getPendingCustomerIds(insightScheduleInfo, insight).getPendingCustomerIds();
        Assert.assertEquals(result.size(), 0);
        verify(mockCustomerClient, times(1)).getObjects(anyList());
    }

    @Test
    public void testGetPendingCustomerIdsClientSuccess() throws Throwable
    {
        List<String> customerIds = Arrays.asList("A", "B", "C");
        DataObjectResponse<Customer> customerFeed = new DefaultDataObjectResponse<>();
        customerFeed.addAll(customerIds.stream().map(this::createCustomer).collect(Collectors.toList()));
        doReturn(customerFeed).when(mockCustomerClient).getObjects(anyList());
        List<String> result = scheduler.getPendingCustomerIds(insightScheduleInfo, insight).getPendingCustomerIds();
        Assert.assertTrue(customerIds.containsAll(result));
        Assert.assertEquals(customerIds.size(), result.size());
        verify(mockCustomerClient, times(1)).getObjects(anyList());
    }

    @Test
    public void testGetPendingCustomerIdsWithPending() throws Throwable
    {
        List<String> customerIds = Arrays.asList("A", "B", "C");
        insightScheduleInfo.setPendingCustomerIds(customerIds);
        List<String> result = scheduler.getPendingCustomerIds(insightScheduleInfo, insight).getPendingCustomerIds();
        Assert.assertTrue(customerIds.containsAll(result));
        Assert.assertEquals(customerIds.size(), result.size());
        verify(mockCustomerClient, times(0)).getObjects(anyList());
    }

    @Test
    public void testScheduleOneReadyAgenda() throws Throwable
    {
        final String EXPECTED_AGENDA_OWNER = "A";
        List<String> customerIds = Arrays.asList(EXPECTED_AGENDA_OWNER, "B", "C");
        insightScheduleInfo.setPendingCustomerIds(customerIds);
        doReturn(createReadyAgendaFeed(EXPECTED_AGENDA_OWNER)).when(mockReadyAgendaPersister).retrieve(anyList());
        List<ReadyAgenda> readyAgendas = scheduler.schedule(1, insight, insightScheduleInfo);
        Assert.assertNotNull(readyAgendas);
        Assert.assertEquals(readyAgendas.size(), 1);
        Assert.assertEquals(EXPECTED_AGENDA_OWNER, readyAgendas.get(0).getCustomerId());
    }

    @Test
    public void testScheduleMultipleCalls() throws Throwable
    {
        final String EXPECTED_AGENDA_OWNER_ONE = "A";
        final String EXPECTED_AGENDA_OWNER_TWO = "B";
        final String EXPECTED_AGENDA_OWNER_THREE = "C";
        List<String> customerIds = Arrays.asList(EXPECTED_AGENDA_OWNER_ONE, EXPECTED_AGENDA_OWNER_TWO, EXPECTED_AGENDA_OWNER_THREE);
        insightScheduleInfo.setPendingCustomerIds(customerIds);
        doAnswer(new Answer()
        {
            int invocationCount = 0;
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                List<Query> queries = (List<Query>)invocationOnMock.getArguments()[0];
                if(queries.size() != 1) Assert.fail("Update the test! Wrong number of input queries.");

                if(invocationCount > 2) return createReadyAgendaFeed(new ArrayList<>(), 0);
                invocationCount++;

                String queryValue = queries.get(0).getValue().toString();
                if(StringUtils.equals(queryValue, insight.getId()+EXPECTED_AGENDA_OWNER_ONE))
                    return createReadyAgendaFeed(EXPECTED_AGENDA_OWNER_ONE);
                if(StringUtils.equals(queryValue, insight.getId()+EXPECTED_AGENDA_OWNER_TWO))
                    return createReadyAgendaFeed(EXPECTED_AGENDA_OWNER_TWO);
                if(StringUtils.equals(queryValue, insight.getId()+EXPECTED_AGENDA_OWNER_THREE))
                    return createReadyAgendaFeed(EXPECTED_AGENDA_OWNER_THREE);
                return null;
            }
        }).when(mockReadyAgendaPersister).retrieve(anyList());
        List<ReadyAgenda> readyAgendas = scheduler.schedule(2, insight, insightScheduleInfo);
        Assert.assertNotNull(readyAgendas);
        Assert.assertEquals(readyAgendas.size(), 2);
        Assert.assertEquals(EXPECTED_AGENDA_OWNER_ONE, readyAgendas.get(0).getCustomerId());
        Assert.assertEquals(EXPECTED_AGENDA_OWNER_TWO, readyAgendas.get(1).getCustomerId());
        Assert.assertEquals(insightScheduleInfo.getPendingCustomerIds().size(), 1);
        Assert.assertEquals(insightScheduleInfo.getPendingCustomerIds().get(0), EXPECTED_AGENDA_OWNER_THREE);
        verify(mockCustomerClient, times(0)).getObjects(anyList());

        // the customers will be queried in the 2nd pass
        DataObjectResponse<Customer> customerResponse = new DefaultDataObjectResponse<>();
        customerResponse.addAll(customerIds.stream()
            .map(customerId -> {
                Customer customer = new Customer();
                customer.setId(customerId);
                return customer;
            })
            .collect(Collectors.toList())
        );
        doReturn(customerResponse).when(mockCustomerClient).getObjects(anyList());
        readyAgendas = scheduler.schedule(2, insight, insightScheduleInfo);
        Assert.assertEquals(readyAgendas.size(), 1);
        Assert.assertEquals(EXPECTED_AGENDA_OWNER_THREE, readyAgendas.get(0).getCustomerId());
        verify(mockCustomerClient, times(1)).getObjects(anyList());
    }

    @Test
    public void testScheduleSecondPass() throws Throwable
    {
        final String EXPECTED_AGENDA_OWNER_ONE = "A";
        final int AGENDA_OWNER_ONE_READY_COUNT = 6;

        final String EXPECTED_AGENDA_OWNER_TWO = "B";
        final int AGENDA_OWNER_TWO_READY_COUNT = 4;

        final String EXPECTED_AGENDA_OWNER_THREE = "C";
        final int AGENDA_OWNER_THREE_READY_COUNT = 9;

        final int EXPECTED_AGENDA_COUNT = AGENDA_OWNER_ONE_READY_COUNT + AGENDA_OWNER_TWO_READY_COUNT + AGENDA_OWNER_THREE_READY_COUNT;

        final String EXPECTED_AGENDA_OWNER_FOUR = "D"; // this owner has no agendas
        List<String> customerIds = Arrays.asList(EXPECTED_AGENDA_OWNER_ONE, EXPECTED_AGENDA_OWNER_TWO, EXPECTED_AGENDA_OWNER_THREE, EXPECTED_AGENDA_OWNER_FOUR);
        // we want to test a Pass 1 that is empty
        insightScheduleInfo.setPendingCustomerIds(Collections.singletonList(EXPECTED_AGENDA_OWNER_FOUR));
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                List<Query> queries = (List<Query>)invocationOnMock.getArguments()[0];
                if(queries.size() != 1) Assert.fail("Update the test! Wrong number of input queries.");

                String queryValue = queries.get(0).getValue().toString();
                if(StringUtils.equals(queryValue, insight.getId()+EXPECTED_AGENDA_OWNER_ONE))
                    return createReadyAgendaFeed(EXPECTED_AGENDA_OWNER_ONE, AGENDA_OWNER_ONE_READY_COUNT);
                if(StringUtils.equals(queryValue, insight.getId()+EXPECTED_AGENDA_OWNER_TWO))
                    return createReadyAgendaFeed(EXPECTED_AGENDA_OWNER_TWO, AGENDA_OWNER_TWO_READY_COUNT);
                if(StringUtils.equals(queryValue, insight.getId()+EXPECTED_AGENDA_OWNER_THREE))
                    return createReadyAgendaFeed(EXPECTED_AGENDA_OWNER_THREE, AGENDA_OWNER_THREE_READY_COUNT);
                if(StringUtils.equals(queryValue, insight.getId()+EXPECTED_AGENDA_OWNER_FOUR))
                    return createReadyAgendaFeed();
                return null;
            }
        }).when(mockReadyAgendaPersister).retrieve(anyList());
        // the customers will be queried in the 2nd pass
        DataObjectResponse<Customer> customerResponse = new DefaultDataObjectResponse<>();
        customerResponse.addAll(customerIds.stream()
            .map(customerId -> {
                Customer customer = new Customer();
                customer.setId(customerId);
                return customer;
            })
            .collect(Collectors.toList())
        );
        doReturn(customerResponse).when(mockCustomerClient).getObjects(anyList());

        List<ReadyAgenda> readyAgendas = scheduler.schedule(EXPECTED_AGENDA_COUNT, insight, insightScheduleInfo);
        Assert.assertNotNull(readyAgendas);
        Assert.assertEquals(readyAgendas.size(), EXPECTED_AGENDA_COUNT);
        verifyAgendaCount(readyAgendas, EXPECTED_AGENDA_OWNER_ONE, AGENDA_OWNER_ONE_READY_COUNT);
        verifyAgendaCount(readyAgendas, EXPECTED_AGENDA_OWNER_TWO, AGENDA_OWNER_TWO_READY_COUNT);
        verifyAgendaCount(readyAgendas, EXPECTED_AGENDA_OWNER_THREE, AGENDA_OWNER_THREE_READY_COUNT);

        Assert.assertTrue(insightScheduleInfo.getPendingCustomerIds().containsAll(Arrays.asList(EXPECTED_AGENDA_OWNER_FOUR)));

        verify(mockCustomerClient, times(1)).getObjects(anyList());
    }

    protected void verifyAgendaCount(List<ReadyAgenda> readyAgendas, String customerId, int expectedCount)
    {
        Assert.assertEquals(readyAgendas.stream()
                .filter(readyAgenda -> StringUtils.equals(customerId, readyAgenda.getCustomerId()))
                .count(),
            expectedCount);
    }
}
