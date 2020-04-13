package com.comcast.pop.endpoint.agenda.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.endpoint.agenda.AgendaRequestProcessor;
import com.comcast.pop.endpoint.agenda.factory.AgendaFactory;
import com.comcast.pop.endpoint.agenda.factory.DefaultAgendaFactory;
import com.comcast.pop.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.comcast.pop.endpoint.base.AbstractServiceRequestProcessor;
import com.comcast.pop.endpoint.base.validation.RequestValidator;
import com.comcast.pop.endpoint.factory.RequestProcessorFactory;
import com.comcast.pop.endpoint.util.ServiceDataRequestResult;
import com.comcast.pop.endpoint.util.ServiceDataObjectRetriever;
import com.comcast.pop.endpoint.util.ServiceResponseFactory;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.RuntimeServiceException;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.agenda.RunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RunAgendaResponse;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.persistence.api.ObjectPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

/**
 * Agenda service request processor for creating agendas with payloads
 * A caller may need to create agendas. We do not want to give access to customer Agenda creation.
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
public class RunAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<RunAgendaResponse, ServiceRequest<RunAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(RunAgendaServiceRequestProcessor.class);

    public static final String INVALID_JSON_PAYLOAD = "Unable to parse input payload as JSON";

    private RequestValidator<ServiceRequest<RunAgendaRequest>> requestValidator = new RunAgendaServiceRequestValidator();
    private RequestProcessorFactory requestProcessorFactory;
    private ServiceResponseFactory<RunAgendaResponse> responseFactory;
    private ServiceDataObjectRetriever<RunAgendaResponse> dataObjectRetriever;

    private JsonHelper jsonHelper = new JsonHelper();

    private ObjectPersister<Customer> customerPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<AgendaTemplate> agendaTemplatePersister;
    private AgendaFactory agendaFactory;

    public RunAgendaServiceRequestProcessor(ObjectPersister<Insight> insightPersister,
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
        responseFactory = new ServiceResponseFactory<>(RunAgendaResponse.class);
        dataObjectRetriever = new ServiceDataObjectRetriever<>(responseFactory);
    }

    @Override
    public RunAgendaResponse processPOST(ServiceRequest<RunAgendaRequest> serviceRequest)
    {
        RunAgendaRequest igniteAgendaRequest = serviceRequest.getPayload();

        JsonNode payloadNode;
        try
        {
            payloadNode = jsonHelper.getObjectMapper().readTree(igniteAgendaRequest.getPayload());
        }
        catch(IOException e)
        {
            return createIgniteAgendaResponse(serviceRequest, null,
                ErrorResponseFactory.badRequest(INVALID_JSON_PAYLOAD, serviceRequest.getCID()), null);
        }

        AgendaTemplateRequestProcessor agendaTemplateRequestProcessor = requestProcessorFactory.createAgendaTemplateRequestProcessor(agendaTemplatePersister);

        // retrieve the AgendaTemplate
        ServiceDataRequestResult<AgendaTemplate, RunAgendaResponse> agendaTemplateResult = dataObjectRetriever.performObjectRetrieve(
            serviceRequest, agendaTemplateRequestProcessor, igniteAgendaRequest.getAgendaTemplateId(), AgendaTemplate.class);
        if(agendaTemplateResult.getServiceResponse() != null)
            return agendaTemplateResult.getServiceResponse();
        AgendaTemplate agendaTemplate = agendaTemplateResult.getDataObjectResponse().getFirst();

        Agenda agendaToCreate = agendaFactory.createAgendaFromObject(agendaTemplate, payloadNode, null, serviceRequest.getCID());

        //create an agenda req with the agenda.customerId for visibility
        DataObjectRequest<Agenda> agendaReqByCustomerId = DefaultDataObjectRequest.customerAuthInstance(agendaToCreate.getCustomerId(), agendaToCreate);
        //create agenda processor with a service level insight visibility
        AgendaRequestProcessor agendaRequestProcessor = requestProcessorFactory.createAgendaRequestProcessor(
            agendaPersister, agendaProgressPersister, readyAgendaPersister, operationProgressPersister, insightPersister, customerPersister);

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
            return createIgniteAgendaResponse(serviceRequest, null,
                ErrorResponseFactory.badRequest(String.format("[%1$s : %2$s]", errorResponse.getTitle(), errorResponse.getDescription()), serviceRequest.getCID()), null);
        }
        return createIgniteAgendaResponse(serviceRequest, createdAgenda, null, null);
    }

    private RunAgendaResponse createIgniteAgendaResponse(ServiceRequest<RunAgendaRequest> serviceRequest, Agenda agenda, ErrorResponse errorResponse, String errorPrefix)
    {
        RunAgendaResponse runAgendaResponse = responseFactory.createResponse(serviceRequest, errorResponse, errorPrefix);
        runAgendaResponse.setAgendas(Collections.singletonList(agenda));
        return runAgendaResponse;
    }

    public RequestValidator<ServiceRequest<RunAgendaRequest>> getRequestValidator()
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
        ServiceDataObjectRetriever<RunAgendaResponse> dataObjectRetriever)
    {
        this.dataObjectRetriever = dataObjectRetriever;
    }

    public void setRequestProcessorFactory(RequestProcessorFactory requestProcessorFactory)
    {
        this.requestProcessorFactory = requestProcessorFactory;
    }
}

