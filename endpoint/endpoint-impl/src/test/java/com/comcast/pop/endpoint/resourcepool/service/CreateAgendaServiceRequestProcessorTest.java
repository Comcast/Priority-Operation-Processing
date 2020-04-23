package com.comcast.pop.endpoint.resourcepool.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.resourcepool.insight.mapper.ParamMapMapper;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.DataObjectFeedServiceResponse;
import com.comcast.pop.endpoint.api.DefaultServiceRequest;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.auth.AuthorizationResponse;
import com.comcast.pop.endpoint.api.auth.DataVisibility;
import com.comcast.pop.endpoint.api.resourcepool.CreateAgendaRequest;
import com.comcast.pop.modules.queue.api.QueueResult;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class CreateAgendaServiceRequestProcessorTest
{
    private CreateAgendaServiceRequestProcessor processor;

    private ServiceRequest<CreateAgendaRequest> createAgendaRequest;
    private ObjectPersister mockInsightPersister = mock(ObjectPersister.class);
    private ObjectPersister mockAgendaPersister = mock(ObjectPersister.class);
    private CreateAgendaRequest mockRequest = Mockito.mock(CreateAgendaRequest.class);
    private static final String ID_RESOURCEPOOL = "ResourcePool ID";
    private static final String ID_RESOURCEPOOL2 = "ResourcePool ID";
    private static final String OWNERID_SERVICE_CALLER = "User's customerID";
    private static final String OWNERID_NOT_SERVICE_CALLER = "NOT the User's customerID";
    private static final String OWNERID_AGENDA = "Agenda customerID";
    private Agenda agenda = new Agenda();

    @BeforeMethod
    public void setup() throws PersistenceException
    {
        agenda = new Agenda();
        agenda.setCustomerId(OWNERID_AGENDA);
        agenda.setParams(new ParamsMap());
        agenda.getParams().put("operation", "x");
        Operation op = new Operation();
        op.setName("my op");
        agenda.setOperations(Collections.singletonList(op));

        doReturn(Collections.singleton(agenda)).when(mockRequest).getAgendas();
        createAgendaRequest = new DefaultServiceRequest<>(mockRequest);
        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, null, DataVisibility.global);
        createAgendaRequest.setAuthorizationResponse(authorizedResponse);

        ObjectPersister<Customer> customerPersister = mock(ObjectPersister.class);
        Customer agendaCustomer = new Customer();
        agendaCustomer.setResourcePoolId(ID_RESOURCEPOOL);
        agendaCustomer.setId(OWNERID_AGENDA);
        agendaCustomer.setCustomerId(OWNERID_AGENDA);
        doReturn(createDataObjectFeed(agendaCustomer)).when(customerPersister).retrieve(anyList());

        ObjectPersister<AgendaProgress> agendaProgressPersister = mock(ObjectPersister.class);
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId("agenda progress ID");
        doReturn(agendaProgress).when(agendaProgressPersister).retrieve(anyString());
        doReturn(agendaProgress).when(agendaProgressPersister).persist(anyObject());

        ObjectPersister<OperationProgress> operationProgressPersister = mock(ObjectPersister.class);
        DataObjectFeed<OperationProgress> opProgressFeed = Mockito.mock(DataObjectFeed.class);
        doReturn(Collections.singletonList(new OperationProgress())).when(opProgressFeed).getAll();
        doReturn(opProgressFeed).when(operationProgressPersister).retrieve(anyList());
        doReturn(new OperationProgress()).when(operationProgressPersister).persist(anyObject());

        ObjectPersister<ReadyAgenda> readyAgendaPersister = mock(ObjectPersister.class);
        doReturn(new ReadyAgenda()).when(readyAgendaPersister).retrieve(anyString());
        doReturn(new ReadyAgenda()).when(readyAgendaPersister).persist(anyObject());

        doReturn(new Agenda()).when(mockAgendaPersister).persist(anyObject());

        processor = new CreateAgendaServiceRequestProcessor(mockInsightPersister, mockAgendaPersister, customerPersister, agendaProgressPersister,
            operationProgressPersister, readyAgendaPersister);
    }

    @Test
    public void testGlobalInsightCallingUserIsNoVisibleToInsight() throws PersistenceException
    {
        //Insight resource pool is not valid for the agenda customer
        Insight insight = new Insight();
        insight.setCustomerId(OWNERID_NOT_SERVICE_CALLER);
        insight.setResourcePoolId(ID_RESOURCEPOOL);
        insight.setIsGlobal(true);
        insight.addMapper(new ParamMapMapper().withMatchValue("operation=x"));

        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, Collections.singleton(OWNERID_SERVICE_CALLER), null);
        createAgendaRequest.setAuthorizationResponse(authorizedResponse);
        DataObjectFeed insightResp = new DataObjectFeed();
        insightResp.add(insight);
        doReturn(insightResp).when(mockInsightPersister).retrieve(anyList());

        //agenda on request has other resource pool
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(createAgendaRequest);
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 0);
        Assert.assertNotNull(getAgendaResponse.getErrorResponse(), getAgendaResponse.getErrorResponse().getDescription());
        Assert.assertTrue(getAgendaResponse.getErrorResponse().getDescription().contains(
            "[ObjectNotFoundException : No available insights for processing agenda]"),
            getAgendaResponse.getErrorResponse().getDescription());
    }
    @Test
    public void testGlobalInsightCallingUserIsVisibleToInsight() throws PersistenceException
    {
        //Insight resource pool is visible for Agenda.customerID
        Insight insight = new Insight();
        insight.addMapper(new ParamMapMapper().withMatchValue("operation=x"));
        insight.setResourcePoolId(ID_RESOURCEPOOL);
        insight.setCustomerId(OWNERID_SERVICE_CALLER);
        insight.setIsGlobal(true);
        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, Collections.singleton(OWNERID_SERVICE_CALLER), null);
        createAgendaRequest.setAuthorizationResponse(authorizedResponse);
        DataObjectFeed insightResp = new DataObjectFeed();
        insightResp.add(insight);
        doReturn(insightResp).when(mockInsightPersister).retrieve(anyList());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(createAgendaRequest);
        Assert.assertNull(getAgendaResponse.getErrorResponse());
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 1);
     }
    @Test
    public void testNonGlobalInsightCallingUserIsNoVisibleToInsight() throws PersistenceException
    {
        //Insight resource pool is not valid for the agenda customer
        Insight insight = new Insight();
        insight.setCustomerId(OWNERID_NOT_SERVICE_CALLER);
        insight.setResourcePoolId(ID_RESOURCEPOOL);
        insight.setIsGlobal(false);
        insight.addMapper(new ParamMapMapper().withMatchValue("operation=x"));

        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, Collections.singleton(OWNERID_SERVICE_CALLER), null);
        createAgendaRequest.setAuthorizationResponse(authorizedResponse);
        DataObjectFeed insightResp = new DataObjectFeed();
        insightResp.add(insight);
        doReturn(insightResp).when(mockInsightPersister).retrieve(anyList());

        //agenda on request has other resource pool
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(createAgendaRequest);
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 0);
        Assert.assertNotNull(getAgendaResponse.getErrorResponse(), getAgendaResponse.getErrorResponse().getDescription());
        Assert.assertTrue(getAgendaResponse.getErrorResponse().getDescription().contains("[ObjectNotFoundException : No available insights for processing agenda]"));
    }
    @Test
    public void testNonGlobalInsightCallingUserIsVisibleToInsightAgendaIsNot() throws PersistenceException
    {
        //Insight resource pool is visible for Agenda.customerID
        Insight insight = new Insight();
        insight.addMapper(new ParamMapMapper().withMatchValue("operation=x"));
        insight.setResourcePoolId(ID_RESOURCEPOOL);
        insight.setCustomerId(OWNERID_SERVICE_CALLER);
        insight.setIsGlobal(false);
        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, Collections.singleton(OWNERID_SERVICE_CALLER), null);
        createAgendaRequest.setAuthorizationResponse(authorizedResponse);
        DataObjectFeed insightResp = new DataObjectFeed();
        insightResp.add(insight);
        doReturn(insightResp).when(mockInsightPersister).retrieve(anyList());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(createAgendaRequest);
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 0);
        Assert.assertNotNull(getAgendaResponse.getErrorResponse(), getAgendaResponse.getErrorResponse().getDescription());
        Assert.assertTrue(getAgendaResponse.getErrorResponse().getDescription().contains("[ObjectNotFoundException : No available insights for processing agenda]"));
    }
    @Test
    public void testNonGlobalInsightCallingUserIsVisibleToInsightAgendaIsInAllowedAccounts() throws PersistenceException
    {
        //Insight resource pool is visible for Agenda.customerID
        Insight insight = new Insight();
        insight.addMapper(new ParamMapMapper().withMatchValue("operation=x"));
        insight.setResourcePoolId(ID_RESOURCEPOOL);
        insight.setCustomerId(OWNERID_SERVICE_CALLER);
        insight.setAllowedCustomerIds(Collections.singleton(OWNERID_AGENDA));
        insight.setIsGlobal(false);
        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, Collections.singleton(OWNERID_SERVICE_CALLER), null);
        createAgendaRequest.setAuthorizationResponse(authorizedResponse);
        DataObjectFeed insightResp = new DataObjectFeed();
        insightResp.add(insight);
        doReturn(insightResp).when(mockInsightPersister).retrieve(anyList());
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.handlePOST(createAgendaRequest);
        Assert.assertNull(getAgendaResponse.getErrorResponse());
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 1);
    }

    private <D> DataObjectFeed<D> createDataObjectFeed(D dataObject)
    {
        DataObjectFeed<D> customerFeed = new DataObjectFeed<>();
        customerFeed.add(dataObject);
        return customerFeed;
    }

    private QueueResult<ReadyAgenda> createQueueResult(boolean successful, Collection<ReadyAgenda> data ,String message)
    {
        return new QueueResult<>(successful, data, message);
    }
}
