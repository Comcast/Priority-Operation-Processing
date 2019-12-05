package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.agenda.factory.AgendaFactory;
import com.theplatform.dfh.cp.endpoint.agenda.factory.DefaultAgendaFactory;
import com.theplatform.dfh.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.cleanup.EndpointObjectTracker;
import com.theplatform.dfh.cp.endpoint.cleanup.ObjectTracker;
import com.theplatform.dfh.cp.endpoint.cleanup.ObjectTrackerManager;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.cleanup.PersisterObjectTracker;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.validation.TransformValidator;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.ByTitle;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.UUID;

/**
 * TransformRequest specific RequestProcessor
 */
public class TransformRequestProcessor extends EndpointDataObjectRequestProcessor<TransformRequest>
{
    private static final Logger logger = LoggerFactory.getLogger(TransformRequestProcessor.class);
    // HACK: until we have a solution for how we want to handler this
    public static final String CREATE_EXEC_PROGRESS_PARAM = "createExecProgress";
    private JsonHelper jsonHelper = new JsonHelper();

    private DataObjectRequestProcessor<AgendaProgress> agendaProgressRequestProcessor;
    private DataObjectRequestProcessor<Agenda> agendaRequestProcessor;
    private DataObjectRequestProcessor<AgendaTemplate> agendaTemplateClient;
    private AgendaFactory agendaFactory;

    public TransformRequestProcessor(
        ObjectPersister<TransformRequest> transformRequestObjectPersister,
        ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister,
        ObjectPersister<AgendaTemplate> agendaTemplatePersister
        )
    {
        super(transformRequestObjectPersister, new TransformValidator());
        agendaProgressRequestProcessor = new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        agendaTemplateClient = new AgendaTemplateRequestProcessor(agendaTemplatePersister);
        agendaRequestProcessor = new AgendaRequestProcessor(
            agendaPersister,
            agendaProgressPersister,
            readyAgendaPersister,
            operationProgressPersister,
            insightPersister,
            customerPersister
        );
        agendaFactory = new DefaultAgendaFactory();
    }

    @Override
    public DataObjectResponse<TransformRequest> handlePOST(DataObjectRequest<TransformRequest> request)
    {
        preProcessTransformRequest(request.getDataObject());

        //We create the transform req first, so we have the ID for progress operations.
        //If progress fails, we need to rollback the transformReq.
        DataObjectResponse<TransformRequest> response = super.handlePOST(request);
        if (response.isError())
            return response;
        TransformRequest transformRequest = response.getFirst();

        ObjectTrackerManager trackerManager = new ObjectTrackerManager();
        trackerManager.register(new EndpointObjectTracker<>(agendaProgressRequestProcessor, AgendaProgress.class, transformRequest.getCustomerId()));
        trackerManager.register(new PersisterObjectTracker<>(getObjectPersister(), TransformRequest.class));
        trackerManager.track(transformRequest);

        if(transformRequest.getParams() == null) transformRequest.setParams(new ParamsMap());

        DataObjectResponse<AgendaTemplate> agendaTemplateResponse = retrieveAgendaTemplate(request.getAuthorizationResponse(), transformRequest, request.getCID());
        AbstractServiceRequestProcessor.addErrorForObjectNotFound(agendaTemplateResponse, AgendaTemplate.class, transformRequest.getId(), request.getCID());
        if(agendaTemplateResponse.isError())
        {
            return new DefaultDataObjectResponse<>(agendaTemplateResponse.getErrorResponse());
        }
        AgendaTemplate agendaTemplate = agendaTemplateResponse.getFirst();

        ////
        // persist the prep/exec progress
        ////
        DataObjectResponse<AgendaProgress> prepAgendaProgressResponse = createAgendaProgress(
            transformRequest.getLinkId(), transformRequest.getExternalId(), transformRequest.getCustomerId(), transformRequest.getCid());
        if (prepAgendaProgressResponse.isError())
        {
            return new DefaultDataObjectResponse<>(prepAgendaProgressResponse.getErrorResponse());
        }
        AgendaProgress prepAgendaProgress = prepAgendaProgressResponse.getFirst();
        trackerManager.track(prepAgendaProgress);
        transformRequest.getParams().put(GeneralParamKey.progressId, prepAgendaProgress.getId());

        // HACK - NOTE: This is a tempish hack so we don't create Exec progress when not needed. This needs to be revisited.
        if(agendaTemplate.getParams() == null ||
            StringUtils.equalsIgnoreCase(agendaTemplate.getParams().getString(CREATE_EXEC_PROGRESS_PARAM, Boolean.TRUE.toString()), Boolean.TRUE.toString()))
        {
            DataObjectResponse<AgendaProgress> execAgendaProgressResponse = createAgendaProgress(
                transformRequest.getLinkId(), transformRequest.getExternalId(), transformRequest.getCustomerId(), transformRequest.getCid());
            if (execAgendaProgressResponse.isError())
            {
                trackerManager.cleanUp();
                return new DefaultDataObjectResponse<>(execAgendaProgressResponse.getErrorResponse());
            }
            AgendaProgress execAgendaProgress = execAgendaProgressResponse.getFirst();
            trackerManager.track(execAgendaProgress);
            transformRequest.getParams().put(GeneralParamKey.execProgressId, execAgendaProgress.getId());
        }

        DataObjectResponse<Agenda> agendaResponse = createAgenda(agendaTemplate, transformRequest, prepAgendaProgressResponse.getFirst(), request.getCID());
        if (agendaResponse.isError())
        {
            trackerManager.cleanUp();
            return new DefaultDataObjectResponse<>(agendaResponse.getErrorResponse());
        }

        // The transformRequest needs to be persisted with the updated fields (TODO: this order of operations is not desirable)
        transformRequest.getParams().put(GeneralParamKey.agendaId, agendaResponse.getFirst().getId());
        try
        {
            objectPersister.update(transformRequest);
        }
        catch(PersistenceException e)
        {
            trackerManager.cleanUp();
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(e, 500, request.getCID()));
        }

        return response;
    }


    @Override
    public DataObjectResponse<TransformRequest> handlePUT(DataObjectRequest<TransformRequest> request)
    {
        throw new BadRequestException("PUT is not implemented for this endpoint");
    }

    private void preProcessTransformRequest(TransformRequest request)
    {
        if(request.getLinkId() == null)
        {
            // TODO: eventually eliminate the externalId usage in the TransformRequest (not necessarily everywhere else)
            if(request.getExternalId() != null)
            {
                request.setLinkId(request.getExternalId());
            }
            else
            {
                request.setLinkId(UUID.randomUUID().toString());
            }
        }
    }

    protected DataObjectResponse<AgendaTemplate> retrieveAgendaTemplate(AuthorizationResponse authorizationResponse, TransformRequest transformRequest, String cid)
    {
        if(transformRequest.getAgendaTemplateId() != null)
        {
            DataObjectRequest<AgendaTemplate> request = new DefaultDataObjectRequest<>(null, transformRequest.getAgendaTemplateId(), null);
            request.setAuthorizationResponse(authorizationResponse);
            return agendaTemplateClient.handleGET(request);
        }
        else if(transformRequest.getAgendaTemplateTitle() != null)
        {
            DataObjectRequest<AgendaTemplate> request = new DefaultDataObjectRequest<>(Collections.singletonList(new ByTitle(transformRequest.getAgendaTemplateTitle())), null,
            null);
            request.setAuthorizationResponse(authorizationResponse);
            return agendaTemplateClient.handleGET(request);
        }
        return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(new RuntimeException("Please specify an AgendaTemplate id or name."), 400, cid));
    }

    private DataObjectResponse<Agenda> createAgenda(AgendaTemplate agendaTemplate, TransformRequest transformRequest, AgendaProgress prepAgendaProgress, String cid)
    {
        ////
        // persist the prepAgenda (this is intentionally last as the Agenda may begin processing immediately)
        ////
        Agenda agenda = agendaFactory.createAgenda(agendaTemplate, transformRequest, prepAgendaProgress.getId(), cid);

        if(agenda == null)
        {
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(
                new RuntimeException("Failed to create Agenda from AgendaTemplate/TransformRequest."), 400, cid));
        }

        DataObjectResponse<Agenda> prepAgendaResponse;
        try
        {
            //If the customer can create transform requests then they are allowed to create agendas.
            //If we don't use a service user authentication then we have to grant all customers write access to agenda. No!
            DataObjectRequest<Agenda> agendaRequest = DefaultDataObjectRequest.serviceUserAuthInstance(agenda);
            prepAgendaResponse = agendaRequestProcessor.handlePOST(agendaRequest);
        }
        catch(Exception e)
        {
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(new RuntimeException("Failed to create connection to persist the Agenda generated " +
                "from the TransformRequest.", e), 400, cid));
        }

        if(prepAgendaResponse == null || prepAgendaResponse.getFirst() == null)
        {
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(new RuntimeException("Failed to create prep Agenda."), 400, cid));
        }
        return prepAgendaResponse;
    }

    private DataObjectResponse<AgendaProgress> createAgendaProgress(String transformRequestId, String externalId, String customerId, String cid)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        // NOTE: link id is the transformRequest id
        agendaProgress.setCid(cid);
        agendaProgress.setCustomerId(customerId);
        agendaProgress.setLinkId(transformRequestId);
        agendaProgress.setExternalId(externalId);
        agendaProgress.setProcessingState(ProcessingState.WAITING);
        logger.debug("Generated AgendaProgress: {}", jsonHelper.getJSONString(agendaProgress));
        try
        {
            //If the customer can create transform requests then they are allowed to create progress.
            //If we don't use a service user authentication then we have to grant all customers write access to progress. No!
            DataObjectRequest<AgendaProgress> request = DefaultDataObjectRequest.serviceUserAuthInstance(agendaProgress);
            return agendaProgressRequestProcessor.handlePOST(request);
        }
        catch(Exception e)
        {
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(
                new RuntimeException(String.format("Failed to persist the Progress TransformRequest: %1$s", transformRequestId), e),
                400, cid));
        }
    }

    @Override
    public RequestValidator<DataObjectRequest<TransformRequest>> getRequestValidator()
    {
        return new TransformValidator();
    }

    public TransformRequestProcessor setAgendaFactory(AgendaFactory agendaFactory)
    {
        this.agendaFactory = agendaFactory;
        return this;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setAgendaProgressRequestProcessor(
        DataObjectRequestProcessor<AgendaProgress> agendaProgressRequestProcessor)
    {
        this.agendaProgressRequestProcessor = agendaProgressRequestProcessor;
    }

    public void setAgendaRequestProcessor(DataObjectRequestProcessor<Agenda> agendaRequestProcessor)
    {
        this.agendaRequestProcessor = agendaRequestProcessor;
    }

    public void setAgendaTemplateClient(DataObjectRequestProcessor<AgendaTemplate> agendaTemplateClient)
    {
        this.agendaTemplateClient = agendaTemplateClient;
    }
}
