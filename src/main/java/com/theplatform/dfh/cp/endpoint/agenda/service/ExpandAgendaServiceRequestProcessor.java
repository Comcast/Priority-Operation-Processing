package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.data.EndpointObjectGenerator;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataObjectRetriever;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataRequestResult;
import com.theplatform.dfh.cp.endpoint.util.ServiceResponseFactory;
import com.theplatform.dfh.cp.endpoint.validation.ExpandAgendaServiceValidator;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.RuntimeServiceException;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.ExpandAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.ExpandAgendaResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Processor for the expand Agenda method for adding additional operations to the Agenda
 */
public class ExpandAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<ExpandAgendaResponse, ServiceRequest<ExpandAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(ExpandAgendaServiceRequestProcessor.class);

    private RequestProcessorFactory requestProcessorFactory;
    private ServiceDataObjectRetriever<ExpandAgendaResponse> serviceDataObjectRetriever;

    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Customer> customerPersister;

    public ExpandAgendaServiceRequestProcessor(ObjectPersister<Agenda> agendaPersister, ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister, ObjectPersister<ReadyAgenda> readyAgendaPersister, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        setRequestValidator(new ExpandAgendaServiceValidator());
        this.agendaPersister = agendaPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        this.operationProgressPersister = operationProgressPersister;
        this.readyAgendaPersister = readyAgendaPersister;
        this.insightPersister = insightPersister;
        this.customerPersister = customerPersister;

        requestProcessorFactory = new RequestProcessorFactory();
        serviceDataObjectRetriever = new ServiceDataObjectRetriever<>(new ServiceResponseFactory<>(ExpandAgendaResponse.class));
    }

    @Override
    public ExpandAgendaResponse processPOST(ServiceRequest<ExpandAgendaRequest> serviceRequest)
    {
        ExpandAgendaRequest expandAgendaRequest = serviceRequest.getPayload();

        AgendaRequestProcessor agendaRequestProcessor = requestProcessorFactory.createAgendaRequestProcessor(
            agendaPersister, agendaProgressPersister, readyAgendaPersister, operationProgressPersister, insightPersister, customerPersister);

        OperationProgressRequestProcessor operationProgressRequestProcessor =
            requestProcessorFactory.createOperationProgressRequestProcessor(operationProgressPersister);

        // Get the Agenda
        ServiceDataRequestResult<Agenda, ExpandAgendaResponse> agendaRequestResult = serviceDataObjectRetriever.performObjectRetrieve(
            serviceRequest, agendaRequestProcessor, expandAgendaRequest.getAgendaId(), Agenda.class);
        if(agendaRequestResult.getServiceResponse() != null)
            return agendaRequestResult.getServiceResponse();
        Agenda agenda = agendaRequestResult.getDataObjectResponse().getFirst();

        ////
        // Update/Persist the Agenda (basically just a pass through to PUT)
        try
        {
            // Updating the AgendaProgress internally updates the OperationProgress
            DataObjectResponse<Agenda> updatedAgendaResponse =
                agendaRequestProcessor.handlePUT(
                    DefaultDataObjectRequest.customerAuthInstance(agenda.getCustomerId(), createUpdatedAgenda(agenda, expandAgendaRequest)));

            if(updatedAgendaResponse.isError())
                return createExpandAgendaResponse(serviceRequest, updatedAgendaResponse.getErrorResponse(), "Failed to update Agenda.");
        }
        catch (Exception e)
        {
            logger.error("Failed to update agenda with generated operations.", e);
            return createExpandAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                new RuntimeServiceException("Failed to update agenda with generated operations.", e, 500), serviceRequest.getCID()), null);
        }

        ////
        // Update/Persist the OperationProgress
        try
        {
            for(OperationProgress opProgress : generateOperationProgressList(agenda, expandAgendaRequest))
            {
                DataObjectResponse<OperationProgress> createOperationProgressResponse =
                    operationProgressRequestProcessor.handlePOST(DefaultDataObjectRequest.customerAuthInstance(opProgress.getCustomerId(), opProgress));

                if(createOperationProgressResponse.isError())
                    return createExpandAgendaResponse(
                        serviceRequest,
                        createOperationProgressResponse.getErrorResponse(),
                        String.format("Failed to create OperationProgress: %1$s", opProgress.getId()));
            }
        }
        catch (Exception e)
        {
            logger.error("Failed to update agenda progress with generated operations.", e);
            return createExpandAgendaResponse(serviceRequest, ErrorResponseFactory.runtimeServiceException(
                new RuntimeServiceException("Failed to update agenda progress with generated operations.", e, 500), serviceRequest.getCID()), null);
        }

        return createExpandAgendaResponse(serviceRequest, null, null);
    }

    private Agenda createUpdatedAgenda(Agenda sourceAgenda, ExpandAgendaRequest expandAgendaRequest)
    {
        // build a sparse agenda for update
        Agenda agenda = new Agenda();
        agenda.setId(sourceAgenda.getId());
        agenda.setCustomerId(sourceAgenda.getCustomerId());
        // append operations
        agenda.setOperations(new LinkedList<>());
        agenda.getOperations().addAll(sourceAgenda.getOperations());
        agenda.getOperations().addAll(expandAgendaRequest.getOperations());
        // append any params
        if(expandAgendaRequest.getParams() != null)
        {
            agenda.setParams(new ParamsMap());
            appendParams(sourceAgenda.getParams(), agenda.getParams());
            appendParams(expandAgendaRequest.getParams(), agenda.getParams());
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

    private List<OperationProgress> generateOperationProgressList(Agenda agenda, ExpandAgendaRequest expandAgendaRequest)
    {
        return expandAgendaRequest.getOperations().stream()
            .map(op -> EndpointObjectGenerator.generateWaitingOperationProgress(agenda, op))
            .collect(Collectors.toList());
    }

    private ExpandAgendaResponse createExpandAgendaResponse(ServiceRequest<ExpandAgendaRequest> serviceRequest, ErrorResponse errorResponse, String errorResponsePrefix)
    {
        if(errorResponsePrefix != null && errorResponse != null && errorResponse.getDescription() != null)
        {
            errorResponse.setDescription(errorResponsePrefix + " " + errorResponse.getDescription());
        }
        ExpandAgendaResponse expandAgendaResponse = new ExpandAgendaResponse();
        expandAgendaResponse.setCID(serviceRequest.getCID());
        expandAgendaResponse.setErrorResponse(errorResponse);
        return expandAgendaResponse;
    }

    public void setRequestProcessorFactory(RequestProcessorFactory requestProcessorFactory)
    {
        this.requestProcessorFactory = requestProcessorFactory;
    }

    public void setServiceDataObjectRetriever(
        ServiceDataObjectRetriever<ExpandAgendaResponse> serviceDataObjectRetriever)
    {
        this.serviceDataObjectRetriever = serviceDataObjectRetriever;
    }
}

