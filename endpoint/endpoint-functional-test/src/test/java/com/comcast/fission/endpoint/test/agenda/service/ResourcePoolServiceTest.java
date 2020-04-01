package com.comcast.fission.endpoint.test.agenda.service;

import com.comcast.fission.endpoint.api.agenda.UpdateAgendaRequest;
import com.comcast.fission.endpoint.api.agenda.UpdateAgendaResponse;
import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.api.resourcepool.GetAgendaRequest;
import com.comcast.fission.endpoint.api.resourcepool.GetAgendaResponse;
import com.comcast.fission.endpoint.api.resourcepool.UpdateAgendaProgressRequest;
import com.comcast.fission.endpoint.api.resourcepool.UpdateAgendaProgressResponse;
import com.comcast.fission.endpoint.test.base.EndpointTestBase;
import com.comcast.fission.endpoint.test.factory.DataGenerator;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import com.theplatform.dfh.http.api.NoAuthHTTPUrlConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

public class ResourcePoolServiceTest extends EndpointTestBase
{

    private static final Logger logger = LoggerFactory.getLogger(ResourcePoolServiceTest.class);
    private final JsonHelper jsonHelper = new JsonHelper();

    @Test(timeOut = 80000L)
    public void testGetAgendaWithInsight() throws InterruptedException
    {
        Insight insight = createTestInsight();

        // insight goes with operation type, so Agenda is put in that insight's queue
        Agenda agenda = DataGenerator.generateAgenda(testCustomerId);
        Operation operation = new Operation();
        operation.setId("op1.id");
        operation.setName("op1.name");
        operation.setType(testInsightOperation);
        operation.setPayload(new HashMap<>());
        agenda.setOperations(Collections.singletonList(operation));
        Agenda createdAgenda = agendaClient.persistObject(agenda).getFirst();
        createdAgenda = agendaClient.getObject(createdAgenda.getId()).getFirst();
        logger.info("New Agenda id: {}", createdAgenda.getId());

        // add progress objects to created lists so they will be cleaned up
        String agendaProgressId = createdAgenda.getProgressId();
        registerProgressObjectsForCleanup(agendaProgressId, createdAgenda.getOperations());

        GetAgendaRequest getAgendaRequest = new GetAgendaRequest(insight.getId(), 1);

        GetAgendaResponse getAgendaResponse = resourcePoolServiceClient.getAgenda(getAgendaRequest);
        while (getAgendaResponse == null || getAgendaResponse.getAgendas() == null || getAgendaResponse.getAgendas().size() == 0) {
            logger.info("getAgenda did not return Agenda. Sleeping and trying again...");
            Thread.sleep(5000);
            getAgendaResponse = resourcePoolServiceClient.getAgenda(getAgendaRequest);
            verifyNoError(getAgendaResponse);
        }

        Assert.assertNotNull(getAgendaResponse.getAgendas().toArray()[0]);
        logger.info("GetAgenda id: {}", getAgendaResponse.getAgendas().toArray()[0]);
    }

    @Test
    void testInvalidInsight()
    {
        GetAgendaRequest getAgendaRequest = new GetAgendaRequest(UUID.randomUUID().toString(), 1);
        GetAgendaResponse getAgendaResponse = resourcePoolServiceClient.getAgenda(getAgendaRequest);
        verifyError(getAgendaResponse, 400, "BadRequestException");
    }

    @Test
    public void testInvalidAuthGetAgenda()
    {
        NoAuthHTTPUrlConnectionFactory noAuthHTTPUrlConnectionFactory = new NoAuthHTTPUrlConnectionFactory();
        ResourcePoolServiceClient fissionClient = new ResourcePoolServiceClient(resourcePoolServiceUrl, noAuthHTTPUrlConnectionFactory);
        GetAgendaResponse getAgendaResponse = fissionClient.getAgenda(new GetAgendaRequest("foo", 1));
        Assert.assertTrue(getAgendaResponse.isError());
        Assert.assertEquals(getAgendaResponse.getErrorResponse().getTitle(), "FissionClientException");
        Assert.assertEquals(getAgendaResponse.getErrorResponse().getResponseCode(), (Integer) 401);
    }

    @Test
    public void testUpdateAgendaProgressWithNewOperation()
    {
        final String EXISTING_OP = "op.1";
        final String GENERATED_OP = "op.2";
        final String PARAMS_MAP_ENTRY = "things";

        DataObjectResponse<Agenda> agendaCreateResponse = agendaClient.persistObject(DataGenerator.createNonRunningLogAgenda(testCustomerId, EXISTING_OP));
        verifyNoError(agendaCreateResponse);
        Agenda createdAgenda = agendaCreateResponse.getFirst();
        createdAgenda = agendaClient.getObject(createdAgenda.getId()).getFirst();

        UpdateAgendaProgressRequest request = new UpdateAgendaProgressRequest();
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId(createdAgenda.getProgressId());
        agendaProgress.setProcessingState(ProcessingState.EXECUTING);

        registerProgressObjectIdsForCleanup(createdAgenda.getProgressId(), Arrays.asList(EXISTING_OP, GENERATED_OP));

        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setAgendaProgressId(createdAgenda.getProgressId());
        operationProgress.setOperation(GENERATED_OP);
        operationProgress.setProcessingState(ProcessingState.EXECUTING);
        operationProgress.setPercentComplete(50d);
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(PARAMS_MAP_ENTRY, "things");
        operationProgress.setParams(paramsMap);
        operationProgress.setCustomerId(createdAgenda.getCustomerId());

        agendaProgress.setOperationProgress(new OperationProgress[] { operationProgress });

        request.setAgendaProgress(agendaProgress);

        final String NEW_OP_ID = OperationProgress.generateId(createdAgenda.getProgressId(), GENERATED_OP);
        DataObjectResponse<OperationProgress> opResponse = operationProgressClient.getObject(NEW_OP_ID);
        Assert.assertTrue(opResponse.getFirst() == null, "Nothing by this operationName should exist.");

        UpdateAgendaProgressResponse updateProgressResponse = resourcePoolServiceClient.updateAgendaProgress(request);
        verifyNoError(updateProgressResponse);

        opResponse = operationProgressClient.getObject(NEW_OP_ID);
        // verify the OperationProgress was generated and all data lines up (roughly, some is provided on the create side in the endpoint code)
        verifyNoError(opResponse);
        OperationProgress retrievedProgress = opResponse.getFirst();
        Assert.assertEquals(retrievedProgress.getPercentComplete(), operationProgress.getPercentComplete());
        Assert.assertEquals(retrievedProgress.getOperation(), operationProgress.getOperation());
        Assert.assertEquals(retrievedProgress.getCustomerId(), operationProgress.getCustomerId());
        Assert.assertEquals(retrievedProgress.getId(), NEW_OP_ID);
        Assert.assertNotNull(retrievedProgress.getParams());
        Assert.assertTrue(retrievedProgress.getParams().containsKey(PARAMS_MAP_ENTRY));

        DataObjectResponse<AgendaProgress> agendaProgressResponse = agendaProgressClient.getObject(createdAgenda.getProgressId());
        verifyNoError(agendaProgressResponse);
        agendaProgress = agendaProgressResponse.getFirst();
        Assert.assertNotNull(agendaProgress.getOperationProgress());
        Assert.assertEquals(agendaProgress.getOperationProgress().length, 2);
    }

    @Test
    public void testUpdateAgendaWithNewOperation()
    {
        final String EXISTING_OP = "op.1";
        final String GENERATED_OP = "op.2";
        final String PARAMS_MAP_ENTRY = "things";

        Agenda createdAgenda = agendaClient.persistObject(DataGenerator.createNonRunningLogAgenda(testCustomerId, EXISTING_OP)).getFirst();
        createdAgenda = agendaClient.getObject(createdAgenda.getId()).getFirst();
        Assert.assertEquals(createdAgenda.getOperations().size(), 1);

        logger.info("Created Agenda: {}", createdAgenda.getId());

        registerProgressObjectIdsForCleanup(createdAgenda.getProgressId(), Arrays.asList(EXISTING_OP, GENERATED_OP));

        UpdateAgendaRequest updateAgendaRequest = new UpdateAgendaRequest();
        updateAgendaRequest.setAgendaId(createdAgenda.getId());
        updateAgendaRequest.setOperations(Arrays.asList(
            DataGenerator.createOperation("sample", GENERATED_OP)
        ));
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(PARAMS_MAP_ENTRY, "");
        updateAgendaRequest.setParams(paramsMap);
        UpdateAgendaResponse expandResonse = resourcePoolServiceClient.updateAgenda(updateAgendaRequest);
        verifyNoError(expandResonse);

        Agenda updatedAgenda = agendaClient.getObject(createdAgenda.getId()).getFirst();
        // verify ops expanded
        Assert.assertEquals(updatedAgenda.getOperations().size(), 2);
        verifyPresence(updatedAgenda.getOperations().stream().map(Operation::getName).collect(Collectors.toSet()),
            Arrays.asList(EXISTING_OP, GENERATED_OP));
        Assert.assertTrue(updatedAgenda.getParams().containsKey(PARAMS_MAP_ENTRY));
        AgendaProgress updatedAgendaProgress = agendaProgressClient.getObject(createdAgenda.getProgressId()).getFirst();
        // verify op progress expanded
        Assert.assertEquals(updatedAgendaProgress.getOperationProgress().length, 2);
        verifyPresence(Arrays.stream(updatedAgendaProgress.getOperationProgress()).map(OperationProgress::getOperation).collect(Collectors.toSet()),
            Arrays.asList(EXISTING_OP, GENERATED_OP));
        Assert.assertNotNull(updatedAgendaProgress.getParams());
        Assert.assertTrue(updatedAgendaProgress.getParams().containsKey(PARAMS_MAP_ENTRY));
    }

    private void verifyPresence(Set<String> set, List<String> expectedEntries)
    {
        for(String entry : expectedEntries)
        {
            Assert.assertTrue(set.contains(entry), String.format("Missing entry in set: %1$s", entry));
        }
    }
}
