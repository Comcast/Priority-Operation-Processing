package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.theplatform.dfh.cp.endpoint.agenda.service.IgniteAgendaServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.cleanup.EndpointObjectTracker;
import com.theplatform.dfh.cp.endpoint.cleanup.ObjectTrackerManager;
import com.theplatform.dfh.cp.endpoint.cleanup.PersisterObjectTracker;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.validation.TransformValidator;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.comcast.fission.endpoint.api.BadRequestException;
import com.comcast.fission.endpoint.api.DefaultServiceRequest;
import com.comcast.fission.endpoint.api.ErrorResponse;
import com.comcast.fission.endpoint.api.ErrorResponseFactory;
import com.comcast.fission.endpoint.api.agenda.IgniteAgendaRequest;
import com.comcast.fission.endpoint.api.agenda.IgniteAgendaResponse;
import com.comcast.fission.endpoint.api.auth.AuthorizationResponse;
import com.comcast.fission.endpoint.api.data.DataObjectRequest;
import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.fission.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.fission.endpoint.api.data.query.ByTitle;
import com.comcast.fission.endpoint.api.data.query.progress.ByLinkId;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    private DataObjectRequestProcessor<AgendaTemplate> agendaTemplateClient;
    private IgniteAgendaServiceRequestProcessor igniteAgendaServiceRequestProcessor;
    private boolean isReexecute;
    private ObjectPersister<TransformRequest> transformRequestObjectPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;

    public TransformRequestProcessor(
        ObjectPersister<TransformRequest> transformRequestObjectPersister,
        ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister,
        ObjectPersister<AgendaTemplate> agendaTemplatePersister,
        boolean isReexecute)
    {
        super(transformRequestObjectPersister, new TransformValidator());
        this.transformRequestObjectPersister = transformRequestObjectPersister;
        this.agendaPersister = agendaPersister;
        this.agendaProgressPersister = agendaProgressPersister;
        agendaProgressRequestProcessor = new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        agendaTemplateClient = new AgendaTemplateRequestProcessor(agendaTemplatePersister);
        igniteAgendaServiceRequestProcessor = new IgniteAgendaServiceRequestProcessor(insightPersister, agendaPersister, customerPersister, agendaProgressPersister,
            operationProgressPersister, readyAgendaPersister, agendaTemplatePersister);
        this.isReexecute = isReexecute;
    }

    @Override
    public DataObjectResponse<TransformRequest> handlePOST(DataObjectRequest<TransformRequest> request)
    {
        preProcessTransformRequest(request.getDataObject());

        TransformRequest originalTransformRequest = null;
        Agenda originalAgenda = null;
        List<AgendaProgress> originalAgendaProgresses = new ArrayList<>();
        if (isReexecute)
        {
            try
            {
                String linkId = request.getDataObject().getLinkId();
                //Retrieve old progress/Agenda, see if it is still in progress
                originalAgendaProgresses = getAgendaProgresses(linkId);
                for (AgendaProgress progress : originalAgendaProgresses)
                {
                    if (progress.getProcessingState().equals(ProcessingState.EXECUTING))
                    {
                        return new DefaultDataObjectResponse<>(
                            new ErrorResponse(
                                new BadRequestException("Agenda is currently processing, cannot reexecute."), 400, request.getCID()));
                    }
                }

                originalTransformRequest = getTransformRequest(linkId);
                originalAgenda = getAgenda(linkId);
            }
            catch (PersistenceException e)
            {
                return new DefaultDataObjectResponse<>(new ErrorResponse(e, 400, request.getCID()));
            }
        }

        //We create the transform req first, so we have the ID for progress operations.
        //If progress fails, we need to rollback the transformReq.
        DataObjectResponse<TransformRequest> response = super.handlePOST(request);
        if (response.isError())
        {
            return response;
        }
        TransformRequest transformRequest = response.getFirst();

        ObjectTrackerManager trackerManager = new ObjectTrackerManager();
        trackerManager.register(new EndpointObjectTracker<>(agendaProgressRequestProcessor, AgendaProgress.class, transformRequest.getCustomerId()));
        trackerManager.register(new PersisterObjectTracker<>(getObjectPersister(), TransformRequest.class));
        trackerManager.track(transformRequest);

        if (transformRequest.getParams() == null)
        {
            transformRequest.setParams(new ParamsMap());
        }

        ////
        // Retrieve the agenda template (Note: This is only done so we can handle the hack for not creating exec when not needed)
        ////
        DataObjectResponse<AgendaTemplate> agendaTemplateResponse = retrieveAgendaTemplate(request.getAuthorizationResponse(), transformRequest, request.getCID());
        AbstractServiceRequestProcessor.addErrorForObjectNotFound(agendaTemplateResponse, AgendaTemplate.class, transformRequest.getId(), request.getCID());
        if(agendaTemplateResponse.isError())
        {
            return new DefaultDataObjectResponse<>(agendaTemplateResponse.getErrorResponse());
        }
        AgendaTemplate agendaTemplate = agendaTemplateResponse.getFirst();

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

        ////
        // Delegate to IgniteAgenda
        ////
        String payload;
        try
        {
            payload = jsonHelper.getObjectMapper().writeValueAsString(transformRequest);
        }
        catch (JsonProcessingException e)
        {
            //should never occur
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.badRequest("Unable to marshall transformRequest", request.getCID()));
        }

        // should just pass templateId or title once lookup is no longer needed
        IgniteAgendaRequest igniteAgendaRequest = new IgniteAgendaRequest(payload, agendaTemplateResponse.getFirst().getId());
        DefaultServiceRequest<IgniteAgendaRequest> igniteServiceRequest = new DefaultServiceRequest<>(igniteAgendaRequest);

        igniteServiceRequest.setCid(request.getCID());
        igniteServiceRequest.setAuthorizationResponse(request.getAuthorizationResponse());

        IgniteAgendaResponse agendaResponse = igniteAgendaServiceRequestProcessor.handlePOST(igniteServiceRequest);

        if (agendaResponse.isError())
        {
            trackerManager.cleanUp();
            return new DefaultDataObjectResponse<>(agendaResponse.getErrorResponse());
        }

        if (isReexecute)
        {
            try {
                //delete old progress/agenda/transformRequest
                deleteOldState(originalAgenda, originalTransformRequest, originalAgendaProgresses);
            }
            catch (PersistenceException e)
            {
                return new DefaultDataObjectResponse<>(new ErrorResponse(e, 400, request.getCID()));
            }
        }

        // The transformRequest needs to be persisted with the updated fields (TODO: this order of operations is not desirable)
        transformRequest.getParams().put(GeneralParamKey.agendaId, agendaResponse.getFirst().getId());
        transformRequest.getParams().put(GeneralParamKey.progressId, agendaResponse.getFirst().getProgressId());
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

    private void deleteOldState(Agenda originalAgenda, TransformRequest originalTransformRequest, List<AgendaProgress> originalAgendaProgresses) throws PersistenceException
    {
        if (originalAgenda != null)
        {
            agendaPersister.delete(originalAgenda.getId());
        }
        if (originalTransformRequest != null)
        {
            transformRequestObjectPersister.delete(originalTransformRequest.getId());
        }
        if (!originalAgendaProgresses.isEmpty())
        {
            for (AgendaProgress progress : originalAgendaProgresses)
            {
                agendaPersister.delete(progress.getId());
            }
        }
    }

    private List<AgendaProgress> getAgendaProgresses(String linkId) throws PersistenceException
    {
        return agendaProgressPersister.retrieve(Collections.singletonList(new ByLinkId(linkId))).getAll();
    }

    private Agenda getAgenda(String linkId) throws PersistenceException
    {
        return first(agendaPersister.retrieve(Collections.singletonList(new ByLinkId(linkId))));
    }

    private TransformRequest getTransformRequest(String linkId) throws PersistenceException
    {
        return first(transformRequestObjectPersister.retrieve(Collections.singletonList(new ByLinkId(linkId))));
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

    private <T> T first(DataObjectFeed<T> feed)
    {
        if(feed.getAll().isEmpty())
        {
            return null;
        }
        return feed.getAll().get(0);
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

    public void setAgendaTemplateClient(DataObjectRequestProcessor<AgendaTemplate> agendaTemplateClient)
    {
        this.agendaTemplateClient = agendaTemplateClient;
    }

    public void setIgniteAgendaServiceRequestProcessor(IgniteAgendaServiceRequestProcessor igniteAgendaServiceRequestProcessor)
    {
        this.igniteAgendaServiceRequestProcessor = igniteAgendaServiceRequestProcessor;
    }
}
