package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agenda.factory.AgendaFactory;
import com.theplatform.dfh.cp.endpoint.agenda.factory.DefaultAgendaFactory;
import com.theplatform.dfh.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataRequestResult;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataObjectRetriever;
import com.theplatform.dfh.cp.endpoint.util.ServiceResponseFactory;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.RuntimeServiceException;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

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
public class SubmitAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<SubmitAgendaResponse, ServiceRequest<SubmitAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(SubmitAgendaServiceRequestProcessor.class);

    public static final String INVALID_JSON_PAYLOAD = "Unable to parse input payload as JSON";

    private RequestValidator<ServiceRequest<SubmitAgendaRequest>> requestValidator = new SubmitAgendaRequestValidator();
    private RequestProcessorFactory requestProcessorFactory;
    private ServiceResponseFactory<SubmitAgendaResponse> responseFactory;
    private ServiceDataObjectRetriever<SubmitAgendaResponse> dataObjectRetriever;

    private JsonHelper jsonHelper = new JsonHelper();

    private ObjectPersister<Customer> customerPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<AgendaTemplate> agendaTemplatePersister;
    private AgendaFactory agendaFactory;

    public SubmitAgendaServiceRequestProcessor(ObjectPersister<Insight> insightPersister,
        ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<Customer> customerPersister, ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister, ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectPersister<AgendaTemplate> agendaTemplatePersister)
    {
        this.insightPersister = insightPersister;
        this.agendaPersister = agendaPersister;
        this.customerPersister = customerPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        this.operationProgressPersister = operationProgressPersister;
        this.readyAgendaPersister = readyAgendaPersister;
        this.agendaFactory = new DefaultAgendaFactory();
        this.agendaTemplatePersister = agendaTemplatePersister;

        requestProcessorFactory = new RequestProcessorFactory();
        responseFactory = new ServiceResponseFactory<>(SubmitAgendaResponse.class);
        dataObjectRetriever = new ServiceDataObjectRetriever<>(responseFactory);
    }

    @Override
    public SubmitAgendaResponse processPOST(ServiceRequest<SubmitAgendaRequest> serviceRequest)
    {
        SubmitAgendaRequest submitAgendaRequest = serviceRequest.getPayload();

        JsonNode payloadNode;
        try
        {
            payloadNode = jsonHelper.getObjectMapper().readTree(submitAgendaRequest.getPayload());
        }
        catch(IOException e)
        {
            return createSubmitAgendaResponse(serviceRequest, null,
                ErrorResponseFactory.badRequest(INVALID_JSON_PAYLOAD, serviceRequest.getCID()), null);
        }

        AgendaTemplateRequestProcessor agendaTemplateRequestProcessor = requestProcessorFactory.createAgendaTemplateRequestProcessor(agendaTemplatePersister);

        // retrieve the AgendaTemplate
        ServiceDataRequestResult<AgendaTemplate, SubmitAgendaResponse> agendaTemplateResult = dataObjectRetriever.performObjectRetrieve(
            serviceRequest, agendaTemplateRequestProcessor, submitAgendaRequest.getAgendaTemplateId(), AgendaTemplate.class);
        if(agendaTemplateResult.getServiceResponse() != null)
            return agendaTemplateResult.getServiceResponse();
        AgendaTemplate agendaTemplate = agendaTemplateResult.getDataObjectResponse().getFirst();

        Agenda agendaToCreate = agendaFactory.createAgendaFromObject(agendaTemplate, payloadNode, null, serviceRequest.getCID());

        //create an agenda req with the agenda.customerId for visibility
        DataObjectRequest<Agenda> agendaReqByCustomerId = DefaultDataObjectRequest.customerAuthInstance(agendaToCreate.getCustomerId(), agendaToCreate);
        //create agenda processor with a service level insight visibility
        AgendaRequestProcessor agendaRequestProcessor = requestProcessorFactory.createAgendaRequestProcessorWithServiceRequestVisibility(
            agendaPersister, agendaProgressPersister, readyAgendaPersister, operationProgressPersister, insightPersister, customerPersister, serviceRequest);

        ErrorResponse errorResponse;
        Agenda createdAgenda = null;
        try
        {
            DataObjectResponse<Agenda> createdAgendaResponse = agendaRequestProcessor.handlePOST(agendaReqByCustomerId);
            errorResponse = createdAgendaResponse.getErrorResponse();
            if (errorResponse == null)
            {
                createdAgenda = createdAgendaResponse.getFirst();
            }
        }
        catch (RuntimeServiceException e)
        {
            errorResponse = ErrorResponseFactory.runtimeServiceException(e, serviceRequest.getCID());
        }

        if(errorResponse != null)
        {
            return createSubmitAgendaResponse(serviceRequest, null,
                ErrorResponseFactory.badRequest(String.format("[%1$s : %2$s]", errorResponse.getTitle(), errorResponse.getDescription()), serviceRequest.getCID()), null);
        }
        return createSubmitAgendaResponse(serviceRequest, createdAgenda, null, null);
    }

    private SubmitAgendaResponse createSubmitAgendaResponse(ServiceRequest<SubmitAgendaRequest> serviceRequest, Agenda agenda, ErrorResponse errorResponse, String errorPrefix)
    {
        SubmitAgendaResponse submitAgendaResponse = responseFactory.createResponse(serviceRequest, errorResponse, errorPrefix);
        submitAgendaResponse.setAgendas(Collections.singletonList(agenda));
        return submitAgendaResponse;
    }

    public RequestValidator<ServiceRequest<SubmitAgendaRequest>> getRequestValidator()
    {
        return requestValidator;
    }

    public void setAgendaFactory(AgendaFactory agendaFactory)
    {
        this.agendaFactory = agendaFactory;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setDataObjectRetriever(
        ServiceDataObjectRetriever<SubmitAgendaResponse> dataObjectRetriever)
    {
        this.dataObjectRetriever = dataObjectRetriever;
    }

    public void setRequestProcessorFactory(RequestProcessorFactory requestProcessorFactory)
    {
        this.requestProcessorFactory = requestProcessorFactory;
    }
}

