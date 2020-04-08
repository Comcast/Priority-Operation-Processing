package com.comcast.pop.cp.endpoint.resourcepool.service;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.UUID;

public class UpdateAgendaProgressServiceRequestProcessorTest
{
    private final String CID = UUID.randomUUID().toString();
    private final String ID = "theId";

    @Test
    public void testCheckForRetrieveErrorFoundError()
    {
        final String MESSAGE = "theMessage";

        ErrorResponse errorResponse = ErrorResponseFactory.badRequest(MESSAGE, CID);
        DefaultDataObjectResponse<AgendaProgress> dataResponse = new DefaultDataObjectResponse<>(errorResponse);
        UpdateAgendaProgressServiceRequestProcessor.addErrorForObjectNotFound(dataResponse, AgendaProgress.class, ID, CID);
        Assert.assertNotNull(dataResponse.getErrorResponse());
        Assert.assertEquals(dataResponse.getErrorResponse().getDescription(), MESSAGE);
        Assert.assertEquals(dataResponse.getErrorResponse().getCorrelationId(), CID);
    }

    @Test
    public void testCheckForRetrieveErrorFoundNoEntries()
    {
        DefaultDataObjectResponse<AgendaProgress> dataResponse = new DefaultDataObjectResponse<>();
       UpdateAgendaProgressServiceRequestProcessor.addErrorForObjectNotFound(dataResponse, AgendaProgress.class, ID, CID);
         Assert.assertNotNull(dataResponse.getErrorResponse());
        Assert.assertTrue(dataResponse.getErrorResponse().getDescription().contains(ID));
        Assert.assertTrue(dataResponse.getErrorResponse().getDescription().contains(AgendaProgress.class.getSimpleName()));
    }

    @Test
    public void testGenerateUpdatedAgendaProgress()
    {
        final String ID = "theId";
        final Double PERCENT = 43d;
        final DiagnosticEvent[] DIAGNOSTICS = new DiagnosticEvent[]{new DiagnosticEvent()};
        final OperationProgress[] OP_PROGRESS = new OperationProgress[]{new OperationProgress()};
        final String PROCESSING_STATE_MESSAGE = "theMessage";
        final ProcessingState PROCESSING_STATE = ProcessingState.EXECUTING;
        final Date STARTED = new Date(0);
        final Date COMPLETED = new Date(1000);

        AgendaProgress existingProgress = new AgendaProgress();
        existingProgress.setId(ID);
        AgendaProgress updatedProgress = new AgendaProgress();
        updatedProgress.setPercentComplete(PERCENT);
        updatedProgress.setDiagnosticEvents(DIAGNOSTICS);
        updatedProgress.setOperationProgress(OP_PROGRESS);
        updatedProgress.setProcessingStateMessage(PROCESSING_STATE_MESSAGE);
        updatedProgress.setProcessingState(PROCESSING_STATE);
        updatedProgress.setStartedTime(STARTED);
        updatedProgress.setCompletedTime(COMPLETED);

        AgendaProgress generatedProgress = UpdateAgendaProgressServiceRequestProcessor.generateUpdatedAgendaProgress("cid", existingProgress, updatedProgress);
        Assert.assertEquals(generatedProgress.getId(), ID);
        Assert.assertEquals(generatedProgress.getPercentComplete(), PERCENT);
        Assert.assertEquals(generatedProgress.getDiagnosticEvents(), DIAGNOSTICS);
        Assert.assertEquals(generatedProgress.getOperationProgress(), OP_PROGRESS);
        Assert.assertEquals(generatedProgress.getProcessingStateMessage(), PROCESSING_STATE_MESSAGE);
        Assert.assertEquals(generatedProgress.getProcessingState(), PROCESSING_STATE);
        Assert.assertEquals(generatedProgress.getStartedTime(), STARTED);
        Assert.assertEquals(generatedProgress.getCompletedTime(), COMPLETED);
    }
}
