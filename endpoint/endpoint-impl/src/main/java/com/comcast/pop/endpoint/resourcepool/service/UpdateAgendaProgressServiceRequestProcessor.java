package com.comcast.pop.endpoint.resourcepool.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.base.AbstractServiceRequestProcessor;
import com.comcast.pop.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.pop.endpoint.resourcepool.InsightRequestProcessor;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.endpoint.api.resourcepool.UpdateAgendaProgressRequest;
import com.comcast.pop.endpoint.api.resourcepool.UpdateAgendaProgressResponse;
import com.comcast.pop.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class UpdateAgendaProgressServiceRequestProcessor extends AbstractServiceRequestProcessor<UpdateAgendaProgressResponse, ServiceRequest<UpdateAgendaProgressRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgendaProgressServiceRequestProcessor.class);

    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<Insight> insightObjectPersister;

    public UpdateAgendaProgressServiceRequestProcessor(
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<OperationProgress> operationProgressPersister,
        ObjectPersister<Insight> insightObjectPersister)
    {
        this.agendaPersister = agendaPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        this.operationProgressPersister = operationProgressPersister;
        this.insightObjectPersister = insightObjectPersister;
    }

    @Override
    public UpdateAgendaProgressResponse processPOST(ServiceRequest<UpdateAgendaProgressRequest> request)
    {
        AgendaProgress updatedAgendaProgress = request.getPayload().getAgendaProgress();
        ErrorResponse errorResponse;
        // Retrieve the AgendaProgress (this is a global visibility request)
        DataObjectResponse<AgendaProgress> agendaProgressResponse = retrieveAgendaProgress(updatedAgendaProgress);
        addErrorForObjectNotFound(agendaProgressResponse, AgendaProgress.class, updatedAgendaProgress.getId(), request.getCID());
        if(agendaProgressResponse.isError()) return new UpdateAgendaProgressResponse(agendaProgressResponse.getErrorResponse());

        // Retrieve the insight (confirms the caller can update this AgendaProgress)
        DataObjectResponse<Insight> insightResponse = retrieveInsight(request, agendaProgressResponse.getFirst().getAgendaInsight().getInsightId());
        addErrorForObjectNotFound(insightResponse, Insight.class, agendaProgressResponse.getFirst().getAgendaInsight().getInsightId(), request.getCID());
        if(insightResponse.isError()) return new UpdateAgendaProgressResponse(insightResponse.getErrorResponse());

        AgendaProgressRequestProcessor agendaProgressRequestProcessor =
            new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        AgendaProgress updatedProgress = generateUpdatedAgendaProgress(request.getCID(), agendaProgressResponse.getFirst(), request.getPayload().getAgendaProgress());
        DataObjectRequest<AgendaProgress> agendaProgressReq = DefaultDataObjectRequest.customerAuthInstance(updatedProgress.getCustomerId(), updatedProgress);
        // NOTE: the put method is used to update an existing AgendaProgress
        DataObjectResponse<AgendaProgress> updateResponse = agendaProgressRequestProcessor.handlePUT(agendaProgressReq);
        return new UpdateAgendaProgressResponse(updateResponse.getErrorResponse());
    }

    private DataObjectResponse<Insight> retrieveInsight(ServiceRequest<UpdateAgendaProgressRequest> request, String insightId)
    {
        InsightRequestProcessor insightRequestProcessor = new InsightRequestProcessor(insightObjectPersister);
        DefaultDataObjectRequest<Insight> insightRequest = new DefaultDataObjectRequest<>();
        insightRequest.setId(insightId);
        insightRequest.setAuthorizationResponse(request.getAuthorizationResponse());
        return insightRequestProcessor.handleGET(insightRequest);
    }

    private DataObjectResponse<AgendaProgress> retrieveAgendaProgress(AgendaProgress agendaProgress)
    {
        AgendaProgressRequestProcessor agendaProgressRequestProcessor = new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);

        DataObjectRequest<AgendaProgress> agendaProgressRequest = DefaultDataObjectRequest.serviceUserAuthInstance(agendaProgress);
        return agendaProgressRequestProcessor.handleGET(agendaProgressRequest);
    }

    /**
     * Creates a new progress based on the existing and updated progress (filtered set)
     * @param currentProgress The progress as it exists now
     * @param updatedProgress The to-be-updated progress object
     * @return
     */
    static AgendaProgress generateUpdatedAgendaProgress(String cid, AgendaProgress currentProgress, AgendaProgress updatedProgress)
    {
        AgendaProgress generatedProgress = new AgendaProgress();
        generatedProgress.setId(currentProgress.getId());
        generatedProgress.setCustomerId(currentProgress.getCustomerId());
        generatedProgress.setPercentComplete(updatedProgress.getPercentComplete());
        generatedProgress.setDiagnosticEvents(updatedProgress.getDiagnosticEvents());
        generatedProgress.setOperationProgress(updatedProgress.getOperationProgress());
        generatedProgress.setProcessingStateMessage(updatedProgress.getProcessingStateMessage());
        generatedProgress.setProcessingState(updatedProgress.getProcessingState());
        generatedProgress.setStartedTime(updatedProgress.getStartedTime());
        generatedProgress.setCompletedTime(updatedProgress.getCompletedTime());
        generatedProgress.setCid(cid);
        return generatedProgress;
    }
}

