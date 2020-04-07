package com.comcast.pop.endpoint.test.resourcepool.service;

import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.resourcepool.CreateAgendaRequest;
import com.comcast.pop.endpoint.api.resourcepool.CreateAgendaResponse;
import com.comcast.pop.endpoint.test.factory.DataGenerator;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.test.base.EndpointTestBase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreateAgendaServiceTest extends EndpointTestBase
{
    @Test
    public void testCreateAgenda()
    {
        Agenda testAgenda = DataGenerator.generateAgenda(testCustomerId);

        CreateAgendaRequest createAgendaRequest = new CreateAgendaRequest();
        createAgendaRequest.setAgendas(Collections.singletonList(testAgenda));

        CreateAgendaResponse response = resourcePoolServiceClient.createAgenda(createAgendaRequest);

        if(response.isError())
        {
            Assert.fail(response.getErrorResponse().getTitle() + "::" + response.getErrorResponse().getDescription());
        }

        List<Agenda> results = new ArrayList<>(response.getAgendas());

        Agenda createdAgenda = results.get(0);
        registerProgressObjectsForCleanup(createdAgenda.getProgressId(), createdAgenda.getOperations());

        verifyCreatedTestObject(results.get(0), testAgenda);
    }

    protected void verifyCreatedTestObject(Agenda createdObject, Agenda testObject)
    {
        final String EXPECTED_CID = testObject.getCid();

        // verify AgendaProgress created
        Assert.assertNotNull(createdObject.getProgressId());
        Assert.assertEquals(createdObject.getCid(), EXPECTED_CID);
        String agendaProgressId = createdObject.getProgressId();

        // verify Agenda fields
        Assert.assertEquals(createdObject.getOperations().size(), testObject.getOperations().size());
        for(int idx = 0; idx < testObject.getOperations().size(); idx++)
        {
            // basic verify
            // TODO: does this test need to verify the entire object?
            Assert.assertEquals(createdObject.getOperations().get(idx).getName(), testObject.getOperations().get(idx).getName());
        }
        Assert.assertEquals(createdObject.getJobId(), testObject.getJobId());
        Assert.assertNotNull(createdObject.getAgendaInsight().getInsightId(), "No insightId was assigned. This should be investigated.");

        DataObjectResponse<AgendaProgress> progressResponse = agendaProgressClient.getObject(agendaProgressId);

        Assert.assertFalse(progressResponse.isError(), "Error retrieving the agendaProgress: " + agendaProgressId);

        AgendaProgress agendaProgress = progressResponse.getFirst();

        // verify agendaProgress
        Assert.assertNotNull(agendaProgress);
        Assert.assertEquals(agendaProgress.getCid(), EXPECTED_CID);
        Assert.assertEquals(agendaProgress.getAgendaId(), createdObject.getId());
        Assert.assertEquals(createdObject.getLinkId(), agendaProgress.getLinkId(), "LinkId did not match on Agenda and AgendaProgress");

        // verify operationProgress created for each operation on agenda
        for (Operation op : createdObject.getOperations())
        {
            OperationProgress opProgress = operationProgressClient.getObject(OperationProgress.generateId(agendaProgressId, op.getName())).getFirst();
            Assert.assertEquals(opProgress.getOperation(), op.getName());
            Assert.assertEquals(opProgress.getCid(), EXPECTED_CID);
        }
    }
}
