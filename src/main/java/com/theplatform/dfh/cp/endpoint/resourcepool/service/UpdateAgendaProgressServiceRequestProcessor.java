package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.auth.DataVisibility;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 */
public class UpdateAgendaProgressServiceRequestProcessor extends RequestProcessor<UpdateAgendaProgressResponse, ServiceRequest<UpdateAgendaProgressRequest>>
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
    protected UpdateAgendaProgressResponse handlePOST(ServiceRequest<UpdateAgendaProgressRequest> request)
    {
        AgendaProgress updatedAgendaProgress = request.getPayload().getAgendaProgress();
        UpdateAgendaProgressResponse response;

        // Retrieve the AgendaProgress (this is a global visibility request)
        DataObjectResponse<AgendaProgress> agendaProgressResponse = retrieveAgendaProgress(updatedAgendaProgress);
        response = checkForRetrieveError(agendaProgressResponse, AgendaProgress.class, updatedAgendaProgress.getId(), request.getCID());
        if(response != null) return response;

        // Retrieve the insight (confirms the caller can update this AgendaProgress)
        DataObjectResponse<Insight> insightResponse = retrieveInsight(request, agendaProgressResponse.getFirst().getAgendaInsight().getInsightId());
        response = checkForRetrieveError(insightResponse, Insight.class, agendaProgressResponse.getFirst().getAgendaInsight().getInsightId(), request.getCID());
        if(response != null) return response;

        AgendaProgressRequestProcessor agendaProgressRequestProcessor =
            new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        DefaultDataObjectRequest<AgendaProgress> agendaProgressRequest =
            generateAgendaProgressRequest(request, generateUpdatedAgendaProgress(agendaProgressResponse.getFirst(), request.getPayload().getAgendaProgress()));
        // NOTE: the put method is used to update an existing AgendaProgress
        DataObjectResponse<AgendaProgress> updateResponse = agendaProgressRequestProcessor.handlePUT(agendaProgressRequest);
        return new UpdateAgendaProgressResponse(updateResponse.getErrorResponse());
    }

    static <T extends IdentifiedObject> UpdateAgendaProgressResponse checkForRetrieveError(DataObjectResponse<T> serviceResponse, Class<T> retrieveClass, String id, String cid)
    {
        if(serviceResponse.isError())
        {
            return new UpdateAgendaProgressResponse(serviceResponse.getErrorResponse());
        }
        if(serviceResponse.getCount() == 0)
        {
            final String message = String.format("The %1$s specified was not found or is not visible: %2$s", retrieveClass.getSimpleName(), id);
            logger.error(message);
            return new UpdateAgendaProgressResponse(ErrorResponseFactory.badRequest(message, cid));
        }
        return null;
    }

    private DataObjectResponse<Insight> retrieveInsight(ServiceRequest<UpdateAgendaProgressRequest> request, String insightId)
    {
        InsightRequestProcessor insightRequestProcessor = new InsightRequestProcessor(insightObjectPersister);
        DefaultDataObjectRequest<Insight> insightRequest = new DefaultDataObjectRequest<>();
        insightRequest.setId(insightId);
        insightRequest.setAuthorizationResponse(request.getAuthorizationResponse());
        return insightRequestProcessor.processGET(insightRequest);
    }

    private DataObjectResponse<AgendaProgress> retrieveAgendaProgress(AgendaProgress agendaProgress)
    {
        AgendaProgressRequestProcessor agendaProgressRequestProcessor = new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        DefaultDataObjectRequest<AgendaProgress> agendaProgressRequest = new DefaultDataObjectRequest<>();
        agendaProgressRequest.setId(agendaProgress.getId());
        agendaProgressRequest.setAuthorizationResponse(new AuthorizationResponse(null, null, null, DataVisibility.global));
        return agendaProgressRequestProcessor.processGET(agendaProgressRequest);
    }

    private DefaultDataObjectRequest<AgendaProgress> generateAgendaProgressRequest(ServiceRequest serviceRequest, AgendaProgress updatedProgress)
    {
        DefaultDataObjectRequest<AgendaProgress> agendaReq = new DefaultDataObjectRequest<>();
        agendaReq.setCid(serviceRequest.getCID());
        // because this is an internally constructed request the id must be set
        agendaReq.setId(updatedProgress.getId());
        agendaReq.setPayload(updatedProgress);
        agendaReq.setAuthorizationResponse(
            new AuthorizationResponse(null, null, Collections.singleton(updatedProgress.getCustomerId()), DataVisibility.authorized_account));
        return agendaReq;
    }

    /**
     * Creates a new progress based on the existing and updated progress (filtered set)
     * @param currentProgress The progress as it exists now
     * @param updatedProgress The to-be-updated progress object
     * @return
     */
    static AgendaProgress generateUpdatedAgendaProgress(AgendaProgress currentProgress, AgendaProgress updatedProgress)
    {
        AgendaProgress generatedProgress = new AgendaProgress();
        generatedProgress.setId(currentProgress.getId());
        generatedProgress.setPercentComplete(updatedProgress.getPercentComplete());
        generatedProgress.setDiagnosticEvents(updatedProgress.getDiagnosticEvents());
        generatedProgress.setOperationProgress(updatedProgress.getOperationProgress());
        generatedProgress.setProcessingStateMessage(updatedProgress.getProcessingStateMessage());
        generatedProgress.setProcessingState(updatedProgress.getProcessingState());
        generatedProgress.setStartedTime(updatedProgress.getStartedTime());
        generatedProgress.setCompletedTime(updatedProgress.getCompletedTime());
        return generatedProgress;
    }
}

