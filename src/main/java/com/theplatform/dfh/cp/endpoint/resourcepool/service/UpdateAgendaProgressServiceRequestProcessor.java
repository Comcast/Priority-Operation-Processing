package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.auth.DataVisibility;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.UpdateAgendaProgressResponse;
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

    public UpdateAgendaProgressServiceRequestProcessor(
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<OperationProgress> operationProgressPersister)
    {
        this.agendaPersister = agendaPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        this.operationProgressPersister = operationProgressPersister;
    }

    @Override
    protected UpdateAgendaProgressResponse handlePOST(ServiceRequest<UpdateAgendaProgressRequest> request)
    {
        AgendaProgressRequestProcessor agendaProgressRequestProcessor = generateAgendaRequestProcessor(request);
        // NOTE: the put method is used to update an existing AgendaProgress
        DataObjectResponse<AgendaProgress> updateResponse =
            agendaProgressRequestProcessor.handlePUT(generateAgendaProgressRequest(request, request.getPayload().getAgendaProgress()));
        UpdateAgendaProgressResponse response = new UpdateAgendaProgressResponse();
        response.setErrorResponse(updateResponse.getErrorResponse());
        return response;
    }

    private DefaultDataObjectRequest<AgendaProgress> generateAgendaProgressRequest(ServiceRequest serviceRequest, AgendaProgress agendaProgress)
    {
        DefaultDataObjectRequest<AgendaProgress> agendaReq = new DefaultDataObjectRequest<>();
        agendaReq.setCid(serviceRequest.getCID());
        agendaReq.setPayload(agendaProgress);
        agendaReq.setAuthorizationResponse(
            new AuthorizationResponse(null, null, Collections.singleton(agendaProgress.getCustomerId()), DataVisibility.authorized_account));
        return agendaReq;
    }

    private AgendaProgressRequestProcessor generateAgendaRequestProcessor(ServiceRequest serviceRequest)
    {
        return new AgendaProgressRequestProcessor(
            agendaProgressPersister, agendaPersister, operationProgressPersister);
    }
}

