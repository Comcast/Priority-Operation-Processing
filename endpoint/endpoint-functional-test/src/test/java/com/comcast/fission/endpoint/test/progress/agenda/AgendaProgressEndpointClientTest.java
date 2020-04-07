package com.comcast.fission.endpoint.test.progress.agenda;

import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.test.BaseEndpointObjectClientTest;
import com.comcast.fission.endpoint.test.factory.DataGenerator;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class AgendaProgressEndpointClientTest extends BaseEndpointObjectClientTest<AgendaProgress>
{
    private static final Logger logger = LoggerFactory.getLogger(AgendaProgressEndpointClientTest.class);
    private final String UPDATED_JOB_ID = "theUpdate";
    private final ProcessingState UPDATED_PROCESSING_STATE = ProcessingState.EXECUTING;
    private final String UPDATED_STATE_MESSAGE = "ExampleStateMessage";
    private final Date UPDATED_START_TIME = new GregorianCalendar(1998, 1, 15, 12, 30).getTime();
    private final String EXTERNAL_ID = "http://theexternal.id";
    private final String DIAGNOSTIC_MESSAGE = "theDiagnostic";
    private final String DIAGNOSTIC_MESSAGE_UPDATE = "theDiagnosticUpdate";
    private URLRequestPerformer urlRequestPerformer = new URLRequestPerformer();

    public AgendaProgressEndpointClientTest()
    {
        super(AgendaProgress.class);
    }

    @Override
    public String getEndpointUrl()
    {
        return agendaProgressUrl;
    }

    @Override
    protected AgendaProgress getTestObject()
    {
        AgendaProgress progress = DataGenerator.getAgendaProgress(testCustomerId);
        progress.setExternalId(EXTERNAL_ID);
        progress.setDiagnosticEvents(new DiagnosticEvent[]{new DiagnosticEvent(DIAGNOSTIC_MESSAGE)});
        return progress;
    }
    @Test
    public void testByLinkId() throws Exception
    {
        HttpObjectClient<AgendaProgress> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        AgendaProgress testObject = getTestObject();
        final String linkID = getClass().getSimpleName() +"mylinkId1234";
        testObject.setLinkId(linkID);
        AgendaProgress agendaProgress = objectClient.persistObject(testObject).getFirst();
        logger.info("New object id: {}", agendaProgress.getId());
        HttpURLConnection urlConnection = getDefaultHttpURLConnectionFactory().getHttpURLConnection(getEndpointUrl() +"?bylinkId=" +linkID);
        urlConnection.setRequestMethod("GET");
        String otherResult = urlRequestPerformer.performURLRequest(urlConnection, (byte[])null);
        logger.info("Object: {}", otherResult);
        Assert.assertNotNull(otherResult);
    }
    // this test returns all of the AgendaProgress (completely unnecessary test)
    @Test(enabled = false)
    public void testGetAll() throws Exception
    {
        HttpObjectClient<AgendaProgress> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        AgendaProgress testObject = getTestObject();
        AgendaProgress agendaProgress = objectClient.persistObject(testObject).getFirst();
        logger.info("New object id: {}", agendaProgress.getId());
        HttpURLConnection urlConnection = getDefaultHttpURLConnectionFactory().getHttpURLConnection(getEndpointUrl());
        urlConnection.setRequestMethod("GET");
        String otherResult = urlRequestPerformer.performURLRequest(urlConnection, (byte[])null);
        logger.info("Object: {}", otherResult);
        Assert.assertNotNull(otherResult);
    }

    @Override
    protected void verifyCreatedTestObject(AgendaProgress created, AgendaProgress testObject)
    {
        Assert.assertEquals(created.getLinkId(), testObject.getLinkId(), "LinkId did not match.");
        Assert.assertEquals(created.getExternalId(), testObject.getExternalId(), "ExternalId did not match.");
        Assert.assertNotNull(created.getDiagnosticEvents());
        Assert.assertEquals(created.getDiagnosticEvents().length, 1);
        Assert.assertEquals(created.getDiagnosticEvents()[0].getMessage(), DIAGNOSTIC_MESSAGE);
    }

    @Override
    protected AgendaProgress updateTestObject(AgendaProgress object)
    {
        object.setLinkId(UPDATED_JOB_ID);
        object.setProcessingState(UPDATED_PROCESSING_STATE);
        object.setProcessingStateMessage(UPDATED_STATE_MESSAGE);
        object.setStartedTime(UPDATED_START_TIME);
        object.setDiagnosticEvents(new DiagnosticEvent[]{new DiagnosticEvent(DIAGNOSTIC_MESSAGE), new DiagnosticEvent(DIAGNOSTIC_MESSAGE_UPDATE)});
        return object;
    }

    @Override
    protected void verifyUpdatedTestObject(AgendaProgress object)
    {
        Assert.assertEquals(object.getLinkId(), UPDATED_JOB_ID);
        Assert.assertEquals(object.getProcessingState(), UPDATED_PROCESSING_STATE);
        Assert.assertEquals(object.getProcessingStateMessage(), UPDATED_STATE_MESSAGE);
        Assert.assertEquals(object.getStartedTime(), UPDATED_START_TIME);
        Assert.assertEquals(object.getExternalId(), EXTERNAL_ID);
        Assert.assertNotNull(object.getDiagnosticEvents());
        Assert.assertEquals(object.getDiagnosticEvents().length, 2);
        Assert.assertEquals(object.getDiagnosticEvents()[0].getMessage(), DIAGNOSTIC_MESSAGE);
        Assert.assertEquals(object.getDiagnosticEvents()[1].getMessage(), DIAGNOSTIC_MESSAGE_UPDATE);
    }

    @Test
    public void testUpdateOperationProgress()
    {
        HttpObjectClient<AgendaProgress> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        AgendaProgress agendaProgress = getTestObject();
        agendaProgress = objectClient.persistObject(agendaProgress).getFirst();

        OperationProgress op1 = createOperationProgress(agendaProgress.getId());
        OperationProgress op2 = createOperationProgress(agendaProgress.getId());

        OperationProgress opToUpdate = new OperationProgress();
        opToUpdate.setId(op1.getId());
        opToUpdate.setProcessingState(ProcessingState.EXECUTING);

        AgendaProgress toUpdate = new AgendaProgress();

        toUpdate.setId(agendaProgress.getId());
        toUpdate.setOperationProgress(new OperationProgress[] {opToUpdate});
        DataObjectResponse<AgendaProgress> updateResponse = objectClient.updateObject(toUpdate, toUpdate.getId());
        verifyNoError(updateResponse);

        // verify OperationProgress was updated
        OperationProgress updatedOp = operationProgressClient.getObject(op1.getId()).getFirst();
        Assert.assertEquals(updatedOp.getProcessingState(), opToUpdate.getProcessingState());
        Assert.assertEquals(updatedOp.getAgendaProgressId(), op1.getAgendaProgressId());

        // verify the other OperationProgress didn't change
        OperationProgress updatedOp2 = operationProgressClient.getObject(op2.getId()).getFirst();
        Assert.assertEquals(updatedOp2.getProcessingState(), op2.getProcessingState());
    }

    @Test
    void testUpdateBogusProgress()
    {
        AgendaProgress toUpdate = new AgendaProgress();
        toUpdate.setId(UUID.randomUUID().toString());
        toUpdate.setLinkId(UUID.randomUUID().toString());

        DataObjectResponse<AgendaProgress> response = agendaProgressClient.updateObject(toUpdate, toUpdate.getId());
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "ObjectNotFoundException");
    }

    @Test
    public void testUpdateWithNonExistentOperationProgress()
    {
        HttpObjectClient<AgendaProgress> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        AgendaProgress agendaProgress = getTestObject();
        agendaProgress = objectClient.persistObject(agendaProgress).getFirst();

        OperationProgress bogusOp = new OperationProgress();
        bogusOp.setId(UUID.randomUUID().toString());
        AgendaProgress toUpdate = new AgendaProgress();
        toUpdate.setId(agendaProgress.getId());
        toUpdate.setOperationProgress(new OperationProgress[] {bogusOp});
        objectClient.updateObject(toUpdate, toUpdate.getId());

        DataObjectResponse<OperationProgress> opProgressResponse = operationProgressClient.getObject(bogusOp.getId());
        Assert.assertNull(opProgressResponse.getFirst());
        Assert.assertFalse(opProgressResponse.isError());
    }

    @Test
    void testGetWithOperationProgresses()
    {
        HttpObjectClient<AgendaProgress> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        AgendaProgress agendaProgress = getTestObject();
        agendaProgress = objectClient.persistObject(agendaProgress).getFirst();

        OperationProgress op1 = createOperationProgress(agendaProgress.getId());
        OperationProgress op2 = createOperationProgress(agendaProgress.getId());
        Set expectedOpIds = Arrays.stream(new OperationProgress[] { op1, op2 }).map(OperationProgress::getId).collect(Collectors.toSet());

        // get AgendaProgress and verify OperationProgress objects are returned
        AgendaProgress retrieved = objectClient.getObject(agendaProgress.getId()).getFirst();
        Assert.assertEquals(retrieved.getOperationProgress().length, expectedOpIds.size());
        OperationProgress[] retrievedOpProgress = retrieved.getOperationProgress();
        for (int i = 0; i < retrieved.getOperationProgress().length; i++)
        {
            Assert.assertTrue(expectedOpIds.contains(retrievedOpProgress[i].getId()),
                String.format("%1$s op progress was not expected!", retrievedOpProgress[i].getId()));
        }
    }

    @Test
    void testDeleteProgressAlsoDeletesOperationProgress()
    {
        HttpObjectClient<AgendaProgress> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        AgendaProgress agendaProgress = getTestObject();
        agendaProgress = objectClient.persistObject(agendaProgress).getFirst();

        OperationProgress op1 = createOperationProgress(agendaProgress.getId());
        OperationProgress op2 = createOperationProgress(agendaProgress.getId());

        // delete AgendaProgress
        objectClient.deleteObject(agendaProgress.getId());

        // verify OperationProgress objects were also deleted
        DataObjectResponse<OperationProgress> opProgressResponse = operationProgressClient.getObject(op1.getId());
        Assert.assertNull(opProgressResponse.getFirst());
        Assert.assertFalse(opProgressResponse.isError());

        DataObjectResponse<OperationProgress> opProgressResponse2 = operationProgressClient.getObject(op2.getId());
        Assert.assertNull(opProgressResponse2.getFirst());
        Assert.assertFalse(opProgressResponse2.isError());
    }

    @Test
    void testAttemptsCompletedIncrements()
    {
        HttpObjectClient<AgendaProgress> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        AgendaProgress agendaProgress = getTestObject();
        DataObjectResponse<AgendaProgress> response = objectClient.persistObject(agendaProgress);
        verifyNoError(response);
        agendaProgress = response.getFirst();
        // null on the attempts completed is fine
        if(agendaProgress.getAttemptsCompleted() != null)
            Assert.assertEquals(agendaProgress.getAttemptsCompleted().intValue(), 0);

        agendaProgress.setProcessingState(ProcessingState.COMPLETE);
        response = objectClient.updateObject(agendaProgress, agendaProgress.getId());
        verifyNoError(response);
        agendaProgress = response.getFirst();
        Assert.assertNotNull(agendaProgress.getAttemptsCompleted());
        Assert.assertEquals(agendaProgress.getAttemptsCompleted().intValue(), 1);
    }

    protected OperationProgress createOperationProgress(String agendaProgressId)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setAgendaProgressId(agendaProgressId);
        operationProgress.setCustomerId(testCustomerId);
        operationProgress.setAttemptCount(1);
        operationProgress.setOperation(UUID.randomUUID().toString());
        operationProgress.setProcessingState(ProcessingState.WAITING);
        operationProgress = operationProgressClient.persistObject(operationProgress).getFirst();
        operationProgress.setId(operationProgress.getId());
        return operationProgress;
    }
}
