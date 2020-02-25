package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.NoOpVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityMethod;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataObjectRetriever;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataRequestResult;
import com.theplatform.dfh.cp.endpoint.util.ServiceResponseFactory;
import com.theplatform.dfh.cp.endpoint.validation.AgendaServiceReigniteValidator;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.RuntimeServiceException;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.ReigniteAgendaParameter;
import com.theplatform.dfh.endpoint.api.agenda.service.ReigniteAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.ReigniteAgendaResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

/**
 * Processor for the retry/rerun/reignite  method for running an agenda that was already run before.
 */
public class ReigniteAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<ReigniteAgendaResponse, ServiceRequest<ReigniteAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(ReigniteAgendaServiceRequestProcessor.class);

    private RequestProcessorFactory requestProcessorFactory;
    private ServiceDataObjectRetriever<ReigniteAgendaResponse> serviceDataObjectRetriever;

    private ProgressResetProcessor progressResetProcessor = new ProgressResetProcessor();
    private RequestValidator<ServiceRequest<ReigniteAgendaRequest>> requestValidator = new AgendaServiceReigniteValidator();

    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Customer> customerPersister;

    public ReigniteAgendaServiceRequestProcessor(ObjectPersister<Agenda> agendaPersister, ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister, ObjectPersister<ReadyAgenda> readyAgendaPersister, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        this.agendaPersister = agendaPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        this.operationProgressPersister = operationProgressPersister;
        this.readyAgendaPersister = readyAgendaPersister;
        this.insightPersister = insightPersister;
        this.customerPersister = customerPersister;

        requestProcessorFactory = new RequestProcessorFactory();
        serviceDataObjectRetriever = new ServiceDataObjectRetriever<>(new ServiceResponseFactory<>(ReigniteAgendaResponse.class));
    }

    @Override
    public ReigniteAgendaResponse processPOST(ServiceRequest<ReigniteAgendaRequest> serviceRequest)
    {
        ReigniteAgendaRequest reigniteAgendaRequest = serviceRequest.getPayload();
        Map<ReigniteAgendaParameter, String> agendaRetryParams = ReigniteAgendaParameter.getParametersMap(reigniteAgendaRequest.getParams());

        AgendaRequestProcessor agendaRequestProcessor = requestProcessorFactory.createAgendaRequestProcessor(agendaPersister, agendaProgressPersister, readyAgendaPersister,
            operationProgressPersister, insightPersister, customerPersister);

        AgendaProgressRequestProcessor agendaProgressRequestProcessor =
            requestProcessorFactory.createAgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        // no special visibility required after the Agenda is known to be visible
        agendaProgressRequestProcessor.setVisibilityFilter(VisibilityMethod.GET, new NoOpVisibilityFilter<>());
        agendaProgressRequestProcessor.setVisibilityFilter(VisibilityMethod.PUT, new NoOpVisibilityFilter<>());

        // Get the Agenda
        ServiceDataRequestResult<Agenda, ReigniteAgendaResponse> agendaRequestResult = serviceDataObjectRetriever.performObjectRetrieve(
            serviceRequest, agendaRequestProcessor, reigniteAgendaRequest.getAgendaId(), Agenda.class);
        if(agendaRequestResult.getServiceResponse() != null)
            return agendaRequestResult.getServiceResponse();
        Agenda agenda = agendaRequestResult.getDataObjectResponse().getFirst();

        // Get the AgendaProgress
        ServiceDataRequestResult<AgendaProgress, ReigniteAgendaResponse> agendaProgressRequestResult = serviceDataObjectRetriever.performObjectRetrieve(
            serviceRequest, agendaProgressRequestProcessor, agenda.getProgressId(), AgendaProgress.class);
        if(agendaProgressRequestResult.getServiceResponse() != null)
            return agendaProgressRequestResult.getServiceResponse();
        AgendaProgress agendaProgress = agendaProgressRequestResult.getDataObjectResponse().getFirst();

        // Reset the progress (as specified)
        progressResetProcessor.resetProgress(agendaProgress, reigniteAgendaRequest, agendaRetryParams);

        // Update the AgendaProgress and OperationProgress
        try
        {
            // Updating the AgendaProgress internally updates the OperationProgress
            DataObjectResponse<AgendaProgress> updateAgendaProgressResponse =
                agendaProgressRequestProcessor.handlePUT(new DefaultDataObjectRequest<>(null, agendaProgress.getId(), agendaProgress));

            if(updateAgendaProgressResponse.isError())
                return createReigniteAgendaResponse(serviceRequest, updateAgendaProgressResponse.getErrorResponse(), "Failed to update AgendaProgress.");
        }
        catch (Exception e)
        {
            logger.error("Failed to update progress for reset. Agenda will not execute.", e);
            return createReigniteAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                new RuntimeServiceException("Failed to update progress for reset.", e, 500), serviceRequest.getCID()), null);
        }

        boolean skipExecution = agendaRetryParams.containsKey(ReigniteAgendaParameter.SKIP_EXECUTION)
            || (agenda.getParams() != null && agenda.getParams().containsKey(GeneralParamKey.doNotRun));

        if(!skipExecution)
        {
            DataObjectResponse<ReadyAgenda> readyAgendaResponse = persistReadyAgenda(agenda.getAgendaInsight().getInsightId(), agenda.getId(),
                agenda.getCustomerId(), serviceRequest.getCID());
            if (readyAgendaResponse.isError())
            {
                return createReigniteAgendaResponse(serviceRequest, readyAgendaResponse.getErrorResponse(), null);
            }
        }

        return createReigniteAgendaResponse(serviceRequest, null, null);
    }

    private DataObjectResponse<ReadyAgenda> persistReadyAgenda(String insightId, String agendaId, String customerId, String cid)
    {
        // This code is 99% duplicated, consider a unified spot
        try
        {
            ReadyAgenda readyAgenda = new ReadyAgenda();
            readyAgenda.setInsightId(insightId);
            readyAgenda.setAdded(new Date());
            readyAgenda.setAgendaId(agendaId);
            readyAgenda.setCustomerId(customerId);
            readyAgendaPersister.persist(readyAgenda);
            return new DefaultDataObjectResponse<>();
        }
        catch (PersistenceException e)
        {
            RuntimeException runtimeException = new RuntimeException("Error persisting ReadyAgenda for Agenda " + agendaId, e);
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(runtimeException, 400, cid));
        }
    }

    private ReigniteAgendaResponse createReigniteAgendaResponse(ServiceRequest<ReigniteAgendaRequest> serviceRequest, ErrorResponse errorResponse, String errorResponsePrefix)
    {
        if(errorResponsePrefix != null && errorResponse != null && errorResponse.getDescription() != null)
        {
            errorResponse.setDescription(errorResponsePrefix + " " + errorResponse.getDescription());
        }
        ReigniteAgendaResponse reigniteAgendaResponse = new ReigniteAgendaResponse();
        reigniteAgendaResponse.setCID(serviceRequest.getCID());
        reigniteAgendaResponse.setErrorResponse(errorResponse);
        return reigniteAgendaResponse;
    }

    @Override
    protected RequestValidator<ServiceRequest<ReigniteAgendaRequest>> getRequestValidator()
    {
        return requestValidator;
    }

    public void setProgressResetProcessor(ProgressResetProcessor progressResetProcessor)
    {
        this.progressResetProcessor = progressResetProcessor;
    }

    public void setRequestProcessorFactory(RequestProcessorFactory requestProcessorFactory)
    {
        this.requestProcessorFactory = requestProcessorFactory;
    }
}

