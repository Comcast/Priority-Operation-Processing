package com.comcast.pop.endpoint.test.agenda;

import com.comcast.pop.endpoint.test.factory.DataGenerator;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.test.BaseEndpointObjectClientTest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

public class AgendaEndpointClientTest extends BaseEndpointObjectClientTest<Agenda>
{
    private final String UPDATED_JOB_ID = "theUpdate";

    private static final Logger logger = LoggerFactory.getLogger(AgendaEndpointClientTest.class);

    public AgendaEndpointClientTest()
    {
        super(Agenda.class);
    }

    @Override
    protected boolean canPerformPUT()
    {
        return true;
    }

    @Override
    public String getEndpointUrl()
    {
        return agendaUrl;
    }

    @Override
    protected Agenda getTestObject()
    {
        return DataGenerator.generateAgenda(testCustomerId);
    }

    @Override
    protected Agenda updateTestObject(Agenda object)
    {
        object.setJobId(UPDATED_JOB_ID);
        return object;
    }

    @Override
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

        AgendaProgress agendaProgress = agendaProgressClient.getObject(agendaProgressId).getFirst();

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

    @Override
    protected void verifyUpdatedTestObject(Agenda object)
    {
        Assert.assertEquals(object.getJobId(), UPDATED_JOB_ID);
    }

    @Override
    public void onCreate(Agenda createdObject)
    {
        if(createdObject == null || StringUtils.isBlank(createdObject.getId()))
            return;

        String agendaProgressId = createdObject.getProgressId();
        // mark progress as created so it can be cleaned up   todo should this just be done in the verification?
        registerProgressObjectsForCleanup(agendaProgressId, createdObject.getOperations());

    }
}
