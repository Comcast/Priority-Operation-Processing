package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressResponse;
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
        ErrorResponse response = UpdateAgendaProgressServiceRequestProcessor.checkForRetrieveError(dataResponse, AgendaProgress.class, ID, CID);
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getDescription(), MESSAGE);
        Assert.assertEquals(response.getCorrelationId(), CID);
    }

    @Test
    public void testCheckForRetrieveErrorFoundNoEntries()
    {
        DefaultDataObjectResponse<AgendaProgress> dataResponse = new DefaultDataObjectResponse<>();
        ErrorResponse response = UpdateAgendaProgressServiceRequestProcessor.checkForRetrieveError(dataResponse, AgendaProgress.class, ID, CID);
         Assert.assertNotNull(response);
        Assert.assertTrue(response.getDescription().contains(ID));
        Assert.assertTrue(response.getDescription().contains(AgendaProgress.class.getSimpleName()));
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

        AgendaProgress generatedProgress = UpdateAgendaProgressServiceRequestProcessor.generateUpdatedAgendaProgress(existingProgress, updatedProgress);
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
