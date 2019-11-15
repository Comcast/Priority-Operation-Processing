package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.AllMatchVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.CustomerVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilter;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.CustomerRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.*;
import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.auth.DataVisibility;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.CreateAgendaResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Agenda service request processor for creating agendas
 * A Resource Pool handler may need to create agendas. We do not want to give access to customer Agenda creation.
 * Instead, they are provisioned by having the Insight.ownerID the Agenda maps to in their authorized account list for the calling user.
 *
 * So, the following must be true:
 *
 * 1. The calling user has an authorized account a that matches the Insight.customerId
 * 2. The Agenda the caller is trying to create maps to an Insight where the above is true
 * 3. The Agenda can only map to an Insight if one of the following is true:
 *     Agenda.customerId is in the Insight.allowedCustomerList
 *     Insight.isGlobal is true
 *
 * We first try to create the Agenda using the Agenda.customerId visibility against the Insight
 * Then we verify the calling user has visibility to that Insight
 */
public class CreateAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<CreateAgendaResponse, ServiceRequest<CreateAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(CreateAgendaServiceRequestProcessor.class);

    private ObjectPersister<Customer> customerPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;

    public CreateAgendaServiceRequestProcessor(ObjectPersister<Insight> insightPersister,
        ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<Customer> customerPersister, ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister, ObjectPersister<ReadyAgenda> readyAgendaPersister)
    {
        this.insightPersister = insightPersister;
        this.agendaPersister = agendaPersister;
        this.customerPersister = customerPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        this.operationProgressPersister = operationProgressPersister;
        this.readyAgendaPersister = readyAgendaPersister;
    }

    @Override
    public CreateAgendaResponse processPOST(ServiceRequest<CreateAgendaRequest> serviceRequest)
    {
        CreateAgendaRequest getAgendaRequest = serviceRequest.getPayload();
        //loop through each agenda and do the insight mapping. 
        //look for visibility to that insight by customerID
        Collection<Agenda> agendasToCreate = getAgendaRequest.getAgendas();
        if(agendasToCreate == null || agendasToCreate.size() == 0)
            return createCreateAgendaResponse(serviceRequest, null, null);

        List<Agenda> createdAgendas = new ArrayList<>();
        List<ErrorResponse> errorResponses = new ArrayList<>();
        for(Agenda agendaToCreate : agendasToCreate)
        {
            if(agendaToCreate == null) continue;
            //create an agenda req with the agenda.customerId for visibility
            DataObjectRequest<Agenda> agendaReqByCustomerId = DefaultDataObjectRequest.customerAuthInstance(agendaToCreate.getCustomerId(), agendaToCreate);
            //create agenda processor with a service level insight visibility
            AgendaRequestProcessor agendaRequestProcessor = generateReqProcessorWithCallersVisibility(serviceRequest);
            try
            {
                DataObjectResponse<Agenda> createdAgendaResponse = agendaRequestProcessor.handlePOST(agendaReqByCustomerId);
                if (createdAgendaResponse == null)
                    continue;
                if (createdAgendaResponse.getErrorResponse() == null)
                {
                    createdAgendas.add(createdAgendaResponse.getFirst());
                }
                else
                {
                    errorResponses.add(createdAgendaResponse.getErrorResponse());
                }
            }
            catch (RuntimeServiceException e)
            {
                errorResponses.add(ErrorResponseFactory.runtimeServiceException(e, serviceRequest.getCID()));
            }
        }
        if(errorResponses.size() > 0)
        {
            return createCreateAgendaResponse(serviceRequest, createdAgendas,
                ErrorResponseFactory.badRequest(errorResponsesToString(errorResponses), serviceRequest.getCID()));
        }
        return createCreateAgendaResponse(serviceRequest, createdAgendas, null);
    }

    private CreateAgendaResponse createCreateAgendaResponse(ServiceRequest<CreateAgendaRequest> serviceRequest, List<Agenda> createdAgendas, ErrorResponse errorResponse)
    {
        CreateAgendaResponse createAgendaResponse = new CreateAgendaResponse(createdAgendas);
        createAgendaResponse.setErrorResponse(errorResponse);
        return createAgendaResponse;
    }

    private String errorResponsesToString(List<ErrorResponse> errorResponses)
    {
        if(errorResponses == null) return "";
        //to string the error responses to report.
        final String errorResponseOutput = "[%s : %s]";
        StringBuilder builder = new StringBuilder();
        errorResponses.forEach(error -> builder.append(String.format(errorResponseOutput, error.getTitle(), error.getDescription())));
        return builder.toString();
    }

    public RequestValidator<ServiceRequest<CreateAgendaRequest>> getRequestValidator()
    {
        return null;
    }

    /**
     * Generate an Agenda Request Processor with added visibility checking for the calling user and the Agenda's Insight
     * @param serviceRequest The calling users service request
     * @return An agenda request processor
     */
    private AgendaRequestProcessor generateReqProcessorWithCallersVisibility(ServiceRequest serviceRequest)
    {
        //we need to set a different visibility policy for the insight request processor when a service request comes in.
        InsightRequestProcessor insightRequestProcessor = InsightRequestProcessor.getServiceInstance(insightPersister, serviceRequest);

        CustomerRequestProcessor customerRequestProcessor = new CustomerRequestProcessor(customerPersister);
        AgendaProgressRequestProcessor agendaProgressRequestProcessor =
            new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        OperationProgressRequestProcessor operationProgressRequestProcessor = new OperationProgressRequestProcessor(operationProgressPersister);
        AgendaRequestProcessor agendaRequestProcessor = new AgendaRequestProcessor(agendaPersister,
            readyAgendaPersister,
            agendaProgressRequestProcessor,
           operationProgressRequestProcessor,
            new InsightSelector(insightRequestProcessor, customerRequestProcessor));
        return agendaRequestProcessor;
    }



}

