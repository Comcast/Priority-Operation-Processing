package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.ParamMapMapper;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.DataObjectFeedServiceResponse;
import com.theplatform.dfh.endpoint.api.DefaultServiceRequest;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.auth.DataVisibility;
import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaRequest;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
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

        doReturn(Collections.singleton(agenda)).when(mockRequest).getAgendas();
        createAgendaRequest = new DefaultServiceRequest<>(mockRequest);
        AuthorizationResponse authorizedResponse = new AuthorizationResponse(null, null, null, DataVisibility.global);
        createAgendaRequest.setAuthorizationResponse(authorizedResponse);

        ObjectPersister<Customer> customerPersister = mock(ObjectPersister.class);
        Customer agendaCustomer = new Customer();
        agendaCustomer.setResourcePoolId(ID_RESOURCEPOOL);
        agendaCustomer.setId(OWNERID_AGENDA);
        agendaCustomer.setCustomerId(OWNERID_AGENDA);
        doReturn(agendaCustomer).when(customerPersister).retrieve(anyString());

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
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(createAgendaRequest);
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 0);
        Assert.assertNotNull(getAgendaResponse.getErrorResponse(), getAgendaResponse.getErrorResponse().getDescription());
        Assert.assertTrue(getAgendaResponse.getErrorResponse().getDescription().contains("[ObjectNotFoundException : No available insights for processing agenda null]"));
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
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(createAgendaRequest);
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
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(createAgendaRequest);
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 0);
        Assert.assertNotNull(getAgendaResponse.getErrorResponse(), getAgendaResponse.getErrorResponse().getDescription());
        Assert.assertTrue(getAgendaResponse.getErrorResponse().getDescription().contains("[ObjectNotFoundException : No available insights for processing agenda null]"));
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
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(createAgendaRequest);
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 0);
        Assert.assertNotNull(getAgendaResponse.getErrorResponse(), getAgendaResponse.getErrorResponse().getDescription());
        Assert.assertTrue(getAgendaResponse.getErrorResponse().getDescription().contains("[ObjectNotFoundException : No available insights for processing agenda null]"));
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
        DataObjectFeedServiceResponse<Agenda> getAgendaResponse = processor.processPOST(createAgendaRequest);
        Assert.assertNull(getAgendaResponse.getErrorResponse());
        Assert.assertNotNull(getAgendaResponse.getAll());
        Assert.assertTrue(getAgendaResponse.getAll().size() == 1);
    }

    private QueueResult<ReadyAgenda> createQueueResult(boolean successful, Collection<ReadyAgenda> data ,String message)
    {
        return new QueueResult<>(successful, data, message);
    }
}
