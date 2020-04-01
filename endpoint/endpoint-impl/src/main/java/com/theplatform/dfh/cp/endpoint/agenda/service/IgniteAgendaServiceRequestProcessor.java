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
import com.comcast.fission.endpoint.api.ErrorResponse;
import com.comcast.fission.endpoint.api.ErrorResponseFactory;
import com.comcast.fission.endpoint.api.RuntimeServiceException;
import com.comcast.fission.endpoint.api.ServiceRequest;
import com.comcast.fission.endpoint.api.agenda.IgniteAgendaRequest;
import com.comcast.fission.endpoint.api.agenda.IgniteAgendaResponse;
import com.comcast.fission.endpoint.api.data.DataObjectRequest;
import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;
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
public class IgniteAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<IgniteAgendaResponse, ServiceRequest<IgniteAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(IgniteAgendaServiceRequestProcessor.class);

    public static final String INVALID_JSON_PAYLOAD = "Unable to parse input payload as JSON";

    private RequestValidator<ServiceRequest<IgniteAgendaRequest>> requestValidator = new IgniteAgendaServiceRequestValidator();
    private RequestProcessorFactory requestProcessorFactory;
    private ServiceResponseFactory<IgniteAgendaResponse> responseFactory;
    private ServiceDataObjectRetriever<IgniteAgendaResponse> dataObjectRetriever;

    private JsonHelper jsonHelper = new JsonHelper();

    private ObjectPersister<Customer> customerPersister;
    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<OperationProgress> operationProgressPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;
    private ObjectPersister<ReadyAgenda> readyAgendaPersister;
    private ObjectPersister<AgendaTemplate> agendaTemplatePersister;
    private AgendaFactory agendaFactory;

    public IgniteAgendaServiceRequestProcessor(ObjectPersister<Insight> insightPersister,
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
        responseFactory = new ServiceResponseFactory<>(IgniteAgendaResponse.class);
        dataObjectRetriever = new ServiceDataObjectRetriever<>(responseFactory);
    }

    @Override
    public IgniteAgendaResponse processPOST(ServiceRequest<IgniteAgendaRequest> serviceRequest)
    {
        IgniteAgendaRequest igniteAgendaRequest = serviceRequest.getPayload();

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
        ServiceDataRequestResult<AgendaTemplate, IgniteAgendaResponse> agendaTemplateResult = dataObjectRetriever.performObjectRetrieve(
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

    private IgniteAgendaResponse createIgniteAgendaResponse(ServiceRequest<IgniteAgendaRequest> serviceRequest, Agenda agenda, ErrorResponse errorResponse, String errorPrefix)
    {
        IgniteAgendaResponse igniteAgendaResponse = responseFactory.createResponse(serviceRequest, errorResponse, errorPrefix);
        igniteAgendaResponse.setAgendas(Collections.singletonList(agenda));
        return igniteAgendaResponse;
    }

    public RequestValidator<ServiceRequest<IgniteAgendaRequest>> getRequestValidator()
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
        ServiceDataObjectRetriever<IgniteAgendaResponse> dataObjectRetriever)
    {
        this.dataObjectRetriever = dataObjectRetriever;
    }

    public void setRequestProcessorFactory(RequestProcessorFactory requestProcessorFactory)
    {
        this.requestProcessorFactory = requestProcessorFactory;
    }
}

