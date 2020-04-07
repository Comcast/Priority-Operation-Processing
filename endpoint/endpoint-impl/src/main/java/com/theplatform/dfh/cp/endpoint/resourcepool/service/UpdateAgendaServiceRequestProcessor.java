package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.comcast.pop.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.data.EndpointObjectGenerator;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataObjectRetriever;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataRequestResult;
import com.theplatform.dfh.cp.endpoint.util.ServiceResponseFactory;
import com.theplatform.dfh.cp.endpoint.validation.UpdateAgendaServiceValidator;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.RuntimeServiceException;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaResponse;
import com.comcast.pop.endpoint.api.auth.AuthorizationResponse;
import com.comcast.pop.endpoint.api.auth.CustomerIdAuthorizationResponse;
import com.comcast.pop.endpoint.api.auth.DataVisibility;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processor for the update Agenda method for adding additional operations to the Agenda (also adjusts progress)
 */
public class UpdateAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<UpdateAgendaResponse, ServiceRequest<UpdateAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgendaServiceRequestProcessor.class);

    private RequestProcessorFactory requestProcessorFactory;
    private ServiceDataObjectRetriever<UpdateAgendaResponse> serviceDataObjectRetriever;

    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Customer> customerPersister;

    public UpdateAgendaServiceRequestProcessor(ObjectPersister<Agenda> agendaPersister, ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister, ObjectPersister<ReadyAgenda> readyAgendaPersister, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        setRequestValidator(new UpdateAgendaServiceValidator());
        this.agendaPersister = agendaPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        this.operationProgressPersister = operationProgressPersister;
        this.readyAgendaPersister = readyAgendaPersister;
        this.insightPersister = insightPersister;
        this.customerPersister = customerPersister;

        requestProcessorFactory = new RequestProcessorFactory();
        serviceDataObjectRetriever = new ServiceDataObjectRetriever<>(new ServiceResponseFactory<>(UpdateAgendaResponse.class));
    }

    @Override
    public UpdateAgendaResponse processPOST(ServiceRequest<UpdateAgendaRequest> serviceRequest)
    {
        UpdateAgendaRequest updateAgendaRequest = serviceRequest.getPayload();

        AgendaRequestProcessor agendaRequestProcessor = requestProcessorFactory.createAgendaRequestProcessor(
            agendaPersister, agendaProgressPersister, readyAgendaPersister, operationProgressPersister, insightPersister, customerPersister);

        AgendaProgressRequestProcessor agendaProgressRequestProcessor = requestProcessorFactory.createAgendaProgressRequestProcessor(
            agendaProgressPersister, agendaPersister, operationProgressPersister);

        OperationProgressRequestProcessor operationProgressRequestProcessor =
            requestProcessorFactory.createOperationProgressRequestProcessor(operationProgressPersister);

        InsightRequestProcessor insightRequestProcessor = requestProcessorFactory.createInsightRequestProcessor(insightPersister);

        // Get the Agenda -- global acccess lookup
        ServiceDataRequestResult<Agenda, UpdateAgendaResponse> agendaRequestResult = serviceDataObjectRetriever.performObjectRetrieve(
            serviceRequest,
            agendaRequestProcessor,
            new AuthorizationResponse(null, null, null, DataVisibility.global),
            updateAgendaRequest.getAgendaId(),
            Agenda.class);
        if(agendaRequestResult.getServiceResponse() != null)
            return agendaRequestResult.getServiceResponse();
        Agenda currentAgenda = agendaRequestResult.getDataObjectResponse().getFirst();

        // Get the Insight -- normal lookup proving access to the ResourcePool in general (pass through auth response)
        ServiceDataRequestResult<Insight, UpdateAgendaResponse> insightRequestResult = serviceDataObjectRetriever.performObjectRetrieve(
            serviceRequest,
            insightRequestProcessor,
            currentAgenda.getAgendaInsight().getInsightId(),
            Insight.class);
        if(insightRequestResult.getServiceResponse() != null)
            return insightRequestResult.getServiceResponse();

        // Get the AgendaProgress -- using customer on agenda as the auth
        ServiceDataRequestResult<AgendaProgress, UpdateAgendaResponse> agendaProgressRequestResult = serviceDataObjectRetriever.performObjectRetrieve(
            serviceRequest,
            agendaProgressRequestProcessor,
            new CustomerIdAuthorizationResponse(currentAgenda.getCustomerId()),
            currentAgenda.getProgressId(),
            AgendaProgress.class);
        if(agendaProgressRequestResult.getServiceResponse() != null)
            return agendaProgressRequestResult.getServiceResponse();
        AgendaProgress currentProgress = agendaProgressRequestResult.getDataObjectResponse().getFirst();

        Agenda resultAgenda = null;

        ////
        // Update/Persist the AgendaProgress
        try
        {
            // Updating the Agenda has no impact on Progress objects
            DataObjectResponse<AgendaProgress> updatedAgendaProgressResponse =
                agendaProgressRequestProcessor.handlePUT(
                    DefaultDataObjectRequest.customerAuthInstance(currentAgenda.getCustomerId(), createUpdatedAgendaProgress(currentProgress, updateAgendaRequest)));

            if(updatedAgendaProgressResponse.isError())
                return createExpandAgendaResponse(serviceRequest, updatedAgendaProgressResponse.getErrorResponse(), "Failed to update AgendaProgress.");
        }
        catch (Exception e)
        {
            logger.error("Failed to update agenda progress with generated operations.", e);
            return createExpandAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                new RuntimeServiceException("Failed to update agenda progress with generated operations.", e, 500), serviceRequest.getCID()), null);
        }

        ////
        // Update/Persist the Agenda (basically just a pass through to PUT)
        try
        {
            // Updating the Agenda has no impact on Progress objects
            DataObjectResponse<Agenda> updatedAgendaResponse =
                agendaRequestProcessor.handlePUT(
                    DefaultDataObjectRequest.customerAuthInstance(currentAgenda.getCustomerId(), createUpdatedAgenda(currentAgenda, updateAgendaRequest)));

            if(updatedAgendaResponse.isError())
                return createExpandAgendaResponse(serviceRequest, updatedAgendaResponse.getErrorResponse(), "Failed to update Agenda.");
            resultAgenda = updatedAgendaResponse.getFirst();
        }
        catch (Exception e)
        {
            logger.error("Failed to update agenda with generated operations.", e);
            return createExpandAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                new RuntimeServiceException("Failed to update agenda with generated operations.", e, 500), serviceRequest.getCID()), null);
        }

        ////
        // Persist the new OperationProgress
        try
        {
            for(OperationProgress opProgress : generateOperationProgressList(currentAgenda, updateAgendaRequest))
            {
                DataObjectResponse<OperationProgress> createOperationProgressResponse =
                    operationProgressRequestProcessor.handlePOST(DefaultDataObjectRequest.customerAuthInstance(opProgress.getCustomerId(), opProgress));

                if(createOperationProgressResponse.isError())
                    return createExpandAgendaResponse(serviceRequest, createOperationProgressResponse.getErrorResponse(),
                        String.format("Failed to create OperationProgress: %1$s", opProgress.getId()));
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to update agenda progress with generated operations.", e);
            return createExpandAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                new RuntimeServiceException("Failed to update agenda progress with generated operations.", e, 500), serviceRequest.getCID()), null);
        }

        return createExpandAgendaResponse(serviceRequest, resultAgenda);
    }

    private AgendaProgress createUpdatedAgendaProgress(AgendaProgress sourceAgendaProgress, UpdateAgendaRequest updateAgendaRequest)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId(sourceAgendaProgress.getId());
        agendaProgress.setCustomerId(sourceAgendaProgress.getCustomerId());
        if(updateAgendaRequest.getParams() != null)
        {
            agendaProgress.setParams(new ParamsMap());
            appendParams(sourceAgendaProgress.getParams(), agendaProgress.getParams());
            appendParams(updateAgendaRequest.getParams(), agendaProgress.getParams());
        }
        return agendaProgress;
    }

    private Agenda createUpdatedAgenda(Agenda sourceAgenda, UpdateAgendaRequest updateAgendaRequest)
    {
        // build a sparse agenda for update
        Agenda agenda = new Agenda();
        agenda.setId(sourceAgenda.getId());
        agenda.setCustomerId(sourceAgenda.getCustomerId());
        // append operations
        agenda.setOperations(new LinkedList<>());
        agenda.getOperations().addAll(sourceAgenda.getOperations());
        agenda.getOperations().addAll(updateAgendaRequest.getOperations());
        // append any params
        if(updateAgendaRequest.getParams() != null)
        {
            agenda.setParams(new ParamsMap());
            appendParams(sourceAgenda.getParams(), agenda.getParams());
            appendParams(updateAgendaRequest.getParams(), agenda.getParams());
        }
        return agenda;
    }

    private void appendParams(ParamsMap sourceParams, ParamsMap destinationParams)
    {
        if(sourceParams == null)
            return;
        for (Map.Entry<String, Object> entry : sourceParams.entrySet())
        {
            destinationParams.put(entry.getKey(), entry.getValue());
        }
    }

    private List<OperationProgress> generateOperationProgressList(Agenda agenda, UpdateAgendaRequest updateAgendaRequest)
    {
        return updateAgendaRequest.getOperations().stream()
            .map(op -> EndpointObjectGenerator.generateWaitingOperationProgress(agenda, op))
            .collect(Collectors.toList());
    }

    private UpdateAgendaResponse createExpandAgendaResponse(ServiceRequest<UpdateAgendaRequest> serviceRequest, Agenda resultingAgenda)
    {
        UpdateAgendaResponse updateAgendaResponse = new UpdateAgendaResponse();
        updateAgendaResponse.setCID(serviceRequest.getCID());
        updateAgendaResponse.setAgenda(resultingAgenda);
        return updateAgendaResponse;
    }

    private UpdateAgendaResponse createExpandAgendaResponse(ServiceRequest<UpdateAgendaRequest> serviceRequest, ErrorResponse errorResponse, String errorResponsePrefix)
    {
        if(errorResponsePrefix != null && errorResponse != null && errorResponse.getDescription() != null)
        {
            errorResponse.setDescription(errorResponsePrefix + " " + errorResponse.getDescription());
        }
        UpdateAgendaResponse updateAgendaResponse = new UpdateAgendaResponse();
        updateAgendaResponse.setCID(serviceRequest.getCID());
        updateAgendaResponse.setErrorResponse(errorResponse);
        return updateAgendaResponse;
    }

    public void setRequestProcessorFactory(RequestProcessorFactory requestProcessorFactory)
    {
        this.requestProcessorFactory = requestProcessorFactory;
    }

    public void setServiceDataObjectRetriever(
        ServiceDataObjectRetriever<UpdateAgendaResponse> serviceDataObjectRetriever)
    {
        this.serviceDataObjectRetriever = serviceDataObjectRetriever;
    }
}

