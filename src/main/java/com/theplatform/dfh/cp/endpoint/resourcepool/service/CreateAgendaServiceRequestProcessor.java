package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
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
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
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
public class CreateAgendaServiceRequestProcessor extends RequestProcessor<CreateAgendaResponse, ServiceRequest<CreateAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(CreateAgendaServiceRequestProcessor.class);

    private ObjectPersister<Customer> customerPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<ResourcePool> resourcePoolPersister;

    public CreateAgendaServiceRequestProcessor(ObjectPersister<ResourcePool> resourcePoolPersister, ObjectPersister<Insight> insightPersister,
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
        this.resourcePoolPersister = resourcePoolPersister;
    }

    @Override
    protected CreateAgendaResponse handlePOST(ServiceRequest<CreateAgendaRequest> serviceRequest)
    {
        CreateAgendaRequest getAgendaRequest = serviceRequest.getPayload();
        //loop through each agenda and do the insight mapping. 
        //look for visibility to that insight by customerID
        Collection<Agenda> agendasToCreate = getAgendaRequest.getAgendas();
        if(agendasToCreate == null || agendasToCreate.size() == 0)
            return new CreateAgendaResponse();
        List<Agenda> createdAgendas = new ArrayList<>();
        List<ErrorResponse> errorResponses = new ArrayList<>();
        for(Agenda agendaToCreate : agendasToCreate)
        {
            if(agendaToCreate == null) continue;
            //create an agenda req with the agenda.customerId for visibility
            DefaultDataObjectRequest<Agenda> agendaReq = generateAgendaReq(serviceRequest, agendaToCreate);
            //create agenda processor with a service level insight visibility
            AgendaRequestProcessor serviceAgendaRequestProcessor = generateAgendaRequestProcessor(serviceRequest);
            DataObjectResponse<Agenda> createdAgendaResponse = serviceAgendaRequestProcessor.handlePOST(agendaReq);
            if(createdAgendaResponse == null) continue;
            if(createdAgendaResponse.getErrorResponse() == null)
            {
                createdAgendas.add(createdAgendaResponse.getFirst());
            }
            else
            {
                errorResponses.add(createdAgendaResponse.getErrorResponse());
            }
        }
        if(errorResponses.size() > 0)
        {
            CreateAgendaResponse createAgendaResponse = new CreateAgendaResponse(ErrorResponseFactory.badRequest(errorResponsesToString(errorResponses), serviceRequest.getCID()));
            createAgendaResponse.setAgendas(createdAgendas);
            return createAgendaResponse;
        }
        return new CreateAgendaResponse(createdAgendas);
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

    private DefaultDataObjectRequest<Agenda> generateAgendaReq(ServiceRequest serviceRequest, Agenda agenda)
    {
        DefaultDataObjectRequest<Agenda> agendaReq = new DefaultDataObjectRequest<>();
        agendaReq.setCid(serviceRequest.getCID());
        agendaReq.setPayload(agenda);
        agendaReq.setAuthorizationResponse(new AuthorizationResponse(null, null, Collections.singleton(agenda.getCustomerId()), DataVisibility.authorized_account));
        return agendaReq;
    }

    /**
     * Generate an Agenda Request Processor with added visibility checking for the calling user and the Agenda's Insight
     * @param serviceRequest The calling users service request
     * @return An agenda request processor
     */
    private AgendaRequestProcessor generateAgendaRequestProcessor(ServiceRequest serviceRequest)
    {
        //we need to set a different visibility policy for the insight request processor when a service request comes in.
        InsightRequestProcessor insightRequestProcessor = new InsightRequestProcessor(insightPersister);
        //get current visibilty filter
        VisibilityFilter defaultFilter = insightRequestProcessor.getVisibilityFilter();
        AllMatchVisibilityFilter allMatchVisibilityFilter = new AllMatchVisibilityFilter()
            .withFilter(defaultFilter)
            .withFilter(new ServiceCallerInsightVisibilityFilter(serviceRequest));
        insightRequestProcessor.setVisibilityFilter(allMatchVisibilityFilter);

        CustomerRequestProcessor customerRequestProcessor = new CustomerRequestProcessor(customerPersister);
        AgendaProgressRequestProcessor agendaProgressRequestProcessor =
            new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        OperationProgressRequestProcessor operationProgressRequestProcessor = new OperationProgressRequestProcessor(operationProgressPersister);
        AgendaRequestProcessor agendaRequestProcessor = new AgendaRequestProcessor(agendaPersister,
            readyAgendaPersister,
            new DataObjectRequestProcessorClient<>(agendaProgressRequestProcessor),
            new DataObjectRequestProcessorClient<>(operationProgressRequestProcessor),
            new InsightSelector(insightRequestProcessor, customerRequestProcessor));
        return agendaRequestProcessor;
    }

    /**
     * On top of the Agenda.customerID --> Insight visibility we need to make sure our service caller's authorized accounts
     * match the Insight.customerID.
     *  So, the following must be true:
     *  *
     *  * 1. The calling user has an authorized account a that matches the Insight.customerId
     *  * 2. The Agenda the caller is trying to create maps to an Insight where the above is true
     *  * 3. The Agenda can only map to an Insight if one of the following is true:
     *  *     Agenda.customerId is in the Insight.allowedCustomerList
     *  *     Insight.isGlobal is true
     */
    private static class ServiceCallerInsightVisibilityFilter extends VisibilityFilter<Insight, DataObjectRequest<Insight>>
    {
        private ServiceRequest serviceRequest;
        private CustomerVisibilityFilter insightCustomerVisibilityFilter = new CustomerVisibilityFilter();

        private ServiceCallerInsightVisibilityFilter(ServiceRequest serviceRequest)
        {
             this.serviceRequest = serviceRequest;
        }

        @Override
        public boolean isVisible(DataObjectRequest<Insight> dataObjectRequest, Insight insight)
        {
            //The insight req processor already verified the agenda.customer to insight visibility.
            //Now we need to verify the service caller customerID has access to the Insight.
            DefaultDataObjectRequest<Insight> serviceCallerInsightReq = generateInsightReq(serviceRequest.getAuthorizationResponse(), insight.getId());
            return insightCustomerVisibilityFilter.isVisible(serviceCallerInsightReq, insight);
        }

        private DefaultDataObjectRequest<Insight> generateInsightReq(AuthorizationResponse authorizationResponse, String insightId)
        {
            DefaultDataObjectRequest<Insight> req = new DefaultDataObjectRequest<>();
            req.setAuthorizationResponse(authorizationResponse);
            req.setId(insightId);
            return req;
        }
    }

}

