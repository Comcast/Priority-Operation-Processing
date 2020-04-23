package com.comcast.pop.endpoint.agenda.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.agenda.service.reset.ProgressResetProcessor;
import com.comcast.pop.endpoint.agenda.service.reset.ProgressResetResult;
import com.comcast.pop.endpoint.agenda.AgendaRequestProcessor;
import com.comcast.pop.endpoint.base.AbstractServiceRequestProcessor;
import com.comcast.pop.endpoint.base.validation.RequestValidator;
import com.comcast.pop.endpoint.base.visibility.NoOpVisibilityFilter;
import com.comcast.pop.endpoint.base.visibility.VisibilityMethod;
import com.comcast.pop.endpoint.factory.RequestProcessorFactory;
import com.comcast.pop.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.comcast.pop.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.pop.endpoint.util.ServiceDataObjectRetriever;
import com.comcast.pop.endpoint.util.ServiceDataRequestResult;
import com.comcast.pop.endpoint.util.ServiceResponseFactory;
import com.comcast.pop.endpoint.validation.AgendaServiceRerunValidator;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.RuntimeServiceException;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaParameter;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RerunAgendaResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Processor for the retry/rerun  method for running an agenda that was already run before.
 */
public class RerunAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<RerunAgendaResponse, ServiceRequest<RerunAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(RerunAgendaServiceRequestProcessor.class);

    private RequestProcessorFactory requestProcessorFactory;
    private ServiceDataObjectRetriever<RerunAgendaResponse> serviceDataObjectRetriever;

    private ProgressResetProcessor progressResetProcessor = new ProgressResetProcessor();
    private RequestValidator<ServiceRequest<RerunAgendaRequest>> requestValidator = new AgendaServiceRerunValidator();

    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Customer> customerPersister;

    public RerunAgendaServiceRequestProcessor(ObjectPersister<Agenda> agendaPersister, ObjectPersister<AgendaProgress> agendaProgressPersister,
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
        serviceDataObjectRetriever = new ServiceDataObjectRetriever<>(new ServiceResponseFactory<>(RerunAgendaResponse.class));
    }

    @Override
    public RerunAgendaResponse processPOST(ServiceRequest<RerunAgendaRequest> serviceRequest)
    {
        RerunAgendaRequest rerunAgendaRequest = serviceRequest.getPayload();
        Map<RerunAgendaParameter, String> agendaRetryParams = RerunAgendaParameter.getParametersMap(rerunAgendaRequest.getParams());

        AgendaRequestProcessor agendaRequestProcessor = requestProcessorFactory.createAgendaRequestProcessor(agendaPersister, agendaProgressPersister, readyAgendaPersister,
            operationProgressPersister, insightPersister, customerPersister);

        AgendaProgressRequestProcessor agendaProgressRequestProcessor =
            requestProcessorFactory.createAgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        // no special visibility required after the Agenda is known to be visible
        agendaProgressRequestProcessor.setVisibilityFilter(VisibilityMethod.GET, new NoOpVisibilityFilter<>());
        agendaProgressRequestProcessor.setVisibilityFilter(VisibilityMethod.PUT, new NoOpVisibilityFilter<>());

        // TODO: Reignite may require the insight lookup to determine visibility (as this could be used by services or direct users)

        // Get the Agenda
        ServiceDataRequestResult<Agenda, RerunAgendaResponse> agendaRequestResult = serviceDataObjectRetriever.performObjectRetrieve(
            serviceRequest, agendaRequestProcessor, rerunAgendaRequest.getAgendaId(), Agenda.class);
        if(agendaRequestResult.getServiceResponse() != null)
            return agendaRequestResult.getServiceResponse();
        Agenda agenda = agendaRequestResult.getDataObjectResponse().getFirst();

        // Get the AgendaProgress
        ServiceDataRequestResult<AgendaProgress, RerunAgendaResponse> agendaProgressRequestResult = serviceDataObjectRetriever.performObjectRetrieve(
            serviceRequest, agendaProgressRequestProcessor, agenda.getProgressId(), AgendaProgress.class);
        if(agendaProgressRequestResult.getServiceResponse() != null)
            return agendaProgressRequestResult.getServiceResponse();
        AgendaProgress agendaProgress = agendaProgressRequestResult.getDataObjectResponse().getFirst();

        // Reset the progress (as specified)
        ProgressResetResult progressResetResult = progressResetProcessor.resetProgress(agenda, agendaProgress, agendaRetryParams);

        ///
        // Update the AgendaProgress (and OperationProgress)
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

        if(progressResetResult.getOperationsToDelete().size() > 0)
        {
            ///
            // Delete any operations as necessary (its op generator was reset)
            try
            {
                agendaRequestProcessor.setVisibilityFilter(VisibilityMethod.PUT, new NoOpVisibilityFilter<>());
                DataObjectResponse<Agenda> updateAgendaResponse =
                    agendaRequestProcessor.handlePUT(new DefaultDataObjectRequest<>(
                        null, agenda.getId(), createAgendaWithRemovedOperations(agenda, progressResetResult.getOperationsToDelete())));
                if(updateAgendaResponse.isError())
                    return createReigniteAgendaResponse(serviceRequest, updateAgendaResponse.getErrorResponse(), "Failed to update Agenda.");

            }
            catch (Exception e)
            {
                logger.error("Failed to remove operation progress due to progress reset. Agenda will not execute.", e);
                return createReigniteAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                    new RuntimeServiceException("Failed to update progress for reset.", e, 500), serviceRequest.getCID()), null);
            }

            ///
            // Delete any operation progress as necessary (its op generator was reset)
            try
            {
                OperationProgressRequestProcessor operationProgressRequestProcessor =
                    requestProcessorFactory.createOperationProgressRequestProcessor(operationProgressPersister);
                // no special visibility required after the Agenda is known to be visible
                operationProgressRequestProcessor.setVisibilityFilter(VisibilityMethod.DELETE, new NoOpVisibilityFilter<>());


                for (String opName : progressResetResult.getOperationsToDelete())
                {
                    DataObjectResponse<OperationProgress> operationProgressResponse =
                        operationProgressRequestProcessor.handleDELETE(new DefaultDataObjectRequest<>(
                            null, OperationProgress.generateId(agendaProgress.getId(), opName), null));
                    if(operationProgressResponse.isError())
                        return createReigniteAgendaResponse(serviceRequest, operationProgressResponse.getErrorResponse(), "Failed to delete OperationProgress.");
                }
            }
            catch (Exception e)
            {
                logger.error("Failed to remove operation progress due to progress reset. Agenda will not execute.", e);
                return createReigniteAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                    new RuntimeServiceException("Failed to update progress for reset.", e, 500), serviceRequest.getCID()), null);
            }
        }

        boolean skipExecution = agendaRetryParams.containsKey(RerunAgendaParameter.SKIP_EXECUTION)
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

    private Agenda createAgendaWithRemovedOperations(Agenda currentAgenda, Set<String> deletedOperations)
    {
        Agenda agenda = new Agenda();
        agenda.setId(currentAgenda.getId());
        agenda.setCustomerId(currentAgenda.getCustomerId());
        agenda.setOperations(
            currentAgenda.getOperations().stream()
                .filter(op ->!deletedOperations.contains(op.getName()))
                .collect(Collectors.toList())
        );
        return agenda;
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

    private RerunAgendaResponse createReigniteAgendaResponse(ServiceRequest<RerunAgendaRequest> serviceRequest, ErrorResponse errorResponse, String errorResponsePrefix)
    {
        if(errorResponsePrefix != null && errorResponse != null && errorResponse.getDescription() != null)
        {
            errorResponse.setDescription(errorResponsePrefix + " " + errorResponse.getDescription());
        }
        RerunAgendaResponse rerunAgendaResponse = new RerunAgendaResponse();
        rerunAgendaResponse.setCID(serviceRequest.getCID());
        rerunAgendaResponse.setErrorResponse(errorResponse);
        return rerunAgendaResponse;
    }

    @Override
    protected RequestValidator<ServiceRequest<RerunAgendaRequest>> getRequestValidator()
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

