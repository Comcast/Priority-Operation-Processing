package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.DefaultEndpointDataObject;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.visibility.NoOpVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityMethod;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.util.DataRequestResult;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.RuntimeServiceException;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaParameter;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.RetryAgendaResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
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
 * Processor for the retry/rerun/special execute/ignite/reignite/???  method for running an agenda that was already run before.
 */
public class RetryAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<RetryAgendaResponse, ServiceRequest<RetryAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(RetryAgendaServiceRequestProcessor.class);

    private RequestProcessorFactory requestProcessorFactory;

    private ProgressResetProcessor progressResetProcessor = new ProgressResetProcessor();

    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Customer> customerPersister;

    public RetryAgendaServiceRequestProcessor(ObjectPersister<Agenda> agendaPersister, ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister, ObjectPersister<ReadyAgenda> readyAgendaPersister, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister, RequestProcessorFactory requestProcessorFactory)
    {
        this.requestProcessorFactory = requestProcessorFactory;
        this.agendaPersister = agendaPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        this.operationProgressPersister = operationProgressPersister;
        this.readyAgendaPersister = readyAgendaPersister;
        this.insightPersister = insightPersister;
        this.customerPersister = customerPersister;
    }

    public RetryAgendaServiceRequestProcessor(ObjectPersister<Agenda> agendaPersister, ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister, ObjectPersister<ReadyAgenda> readyAgendaPersister, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        this(agendaPersister, agendaProgressPersister, operationProgressPersister, readyAgendaPersister, insightPersister, customerPersister, new RequestProcessorFactory());
    }

    @Override
    public RetryAgendaResponse processPOST(ServiceRequest<RetryAgendaRequest> serviceRequest)
    {
        RetryAgendaRequest retryAgendaRequest = serviceRequest.getPayload();
        Map<RetryAgendaParameter, String> agendaRetryParams = RetryAgendaParameter.getParametersMap(retryAgendaRequest.getParams());

        AgendaRequestProcessor agendaRequestProcessor = requestProcessorFactory.createAgendaRequestProcessor(agendaPersister, agendaProgressPersister, readyAgendaPersister,
            operationProgressPersister, insightPersister, customerPersister);

        AgendaProgressRequestProcessor agendaProgressRequestProcessor =
            requestProcessorFactory.createAgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        // no special visibility required after the Agenda is known to be visible
        agendaProgressRequestProcessor.setVisibilityFilter(VisibilityMethod.GET, new NoOpVisibilityFilter<>());
        agendaProgressRequestProcessor.setVisibilityFilter(VisibilityMethod.PUT, new NoOpVisibilityFilter<>());

        // Get the Agenda
        DataRequestResult<Agenda, RetryAgendaResponse> agendaRequestResult = performObjectRetrieve(
            serviceRequest, agendaRequestProcessor, retryAgendaRequest.getAgendaId(), Agenda.class);
        if(agendaRequestResult.getServiceResponse() != null)
            return agendaRequestResult.getServiceResponse();
        Agenda agenda = agendaRequestResult.getDataObjectResponse().getFirst();

        // Get the AgendaProgress
        DataRequestResult<AgendaProgress, RetryAgendaResponse> agendaProgressRequestResult = performObjectRetrieve(
            serviceRequest, agendaProgressRequestProcessor, agenda.getProgressId(), AgendaProgress.class);
        if(agendaProgressRequestResult.getServiceResponse() != null)
            return agendaProgressRequestResult.getServiceResponse();
        AgendaProgress agendaProgress = agendaProgressRequestResult.getDataObjectResponse().getFirst();

        // Reset the progress (as specified)
        progressResetProcessor.resetProgress(agendaProgress, retryAgendaRequest, agendaRetryParams);

        // Update the AgendaProgress and OperationProgress
        try
        {
            // Updating the AgendaProgress internally updates the OperationProgress
            DataObjectResponse<AgendaProgress> updateAgendaProgressResponse =
                agendaProgressRequestProcessor.handlePUT(new DefaultDataObjectRequest<>(null, agendaProgress.getId(), agendaProgress));

            if(updateAgendaProgressResponse.isError())
                return createRetryAgendaResponse(serviceRequest, updateAgendaProgressResponse.getErrorResponse(), "Failed to update AgendaProgress.");
        }
        catch (Exception e)
        {
            logger.error("Failed to update progress for reset. Agenda will not execute.", e);
            return createRetryAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                new RuntimeServiceException("Failed to update progress for reset.", e, 500), serviceRequest.getCID()), null);
        }

        if(!agendaRetryParams.containsKey(RetryAgendaParameter.SKIP_EXECUTION))
        {
            DataObjectResponse<ReadyAgenda> readyAgendaResponse = persistReadyAgenda(agenda.getAgendaInsight().getInsightId(), agenda.getId(),
                agenda.getCustomerId(), serviceRequest.getCID());
            if (readyAgendaResponse.isError())
            {
                return createRetryAgendaResponse(serviceRequest, readyAgendaResponse.getErrorResponse(), null);
            }
        }

        return createRetryAgendaResponse(serviceRequest, null, null);
    }

    /**
     * Wrapper around simple retrieves that may result in failure, missing, or success
     * TODO: if this is useful, move it out of this class! (the response type would need to be templated)
     * @param serviceRequest The service request to pull the request details from
     * @param requestProcessor The processor to perform the retrieve with
     * @param objectId The id of the object to lookup
     * @param objectClass The class of the type of object to retrieve
     * @param <D> The type of object to retrieve
     * @return DataRequestResult containing either the object response (good) or a service response(bad)
     */
    private <D extends DefaultEndpointDataObject> DataRequestResult<D, RetryAgendaResponse> performObjectRetrieve(
        ServiceRequest<RetryAgendaRequest> serviceRequest, EndpointDataObjectRequestProcessor<D> requestProcessor,
        String objectId, Class<D> objectClass)
    {
        DataObjectRequest<D> dataObjectRequest = new DefaultDataObjectRequest<>(null, objectId, null);

        // just pass through from the original caller
        dataObjectRequest.setAuthorizationResponse(serviceRequest.getAuthorizationResponse());

        DataObjectResponse<D> dataObjectResponse = requestProcessor.handleGET(dataObjectRequest);
        DataRequestResult<D, RetryAgendaResponse> dataRequestResult = new DataRequestResult<>();
        if(dataObjectResponse.isError())
        {
            dataRequestResult.setServiceResponse(createRetryAgendaResponse(serviceRequest,
                dataObjectResponse.getErrorResponse(), String.format("%1$s %2$s retrieve failed", objectClass.getSimpleName(), objectId)));
        }
        else if(dataObjectResponse.getFirst() == null)
        {
            dataRequestResult.setServiceResponse(createRetryAgendaResponse(serviceRequest,
                ErrorResponseFactory.objectNotFound(
                    String.format("%1$s %2$s not found", objectClass.getSimpleName(), objectId), serviceRequest.getCID()),
                null));
        }
        else
        {
            dataRequestResult.setDataObjectResponse(dataObjectResponse);
        }
        return dataRequestResult;
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

    private RetryAgendaResponse createRetryAgendaResponse(ServiceRequest<RetryAgendaRequest> serviceRequest, ErrorResponse errorResponse, String errorResponsePrefix)
    {
        if(errorResponsePrefix != null && errorResponse != null && errorResponse.getDescription() != null)
        {
            errorResponse.setDescription(errorResponsePrefix + " " + errorResponse.getDescription());
        }
        RetryAgendaResponse retryAgendaResponse = new RetryAgendaResponse();
        retryAgendaResponse.setCID(serviceRequest.getCID());
        retryAgendaResponse.setErrorResponse(errorResponse);
        return retryAgendaResponse;
    }

    public void setProgressResetProcessor(ProgressResetProcessor progressResetProcessor)
    {
        this.progressResetProcessor = progressResetProcessor;
    }
}

