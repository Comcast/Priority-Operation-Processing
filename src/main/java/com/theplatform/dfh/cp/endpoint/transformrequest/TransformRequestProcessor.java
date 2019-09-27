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
import com.theplatform.dfh.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agendatemplate.map.AgendaTemplateMapperFactory;
import com.theplatform.dfh.cp.endpoint.base.EndpointDataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.cleanup.EndpointObjectTracker;
import com.theplatform.dfh.cp.endpoint.cleanup.ObjectTracker;
import com.theplatform.dfh.cp.endpoint.cleanup.ObjectTrackerManager;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.transformrequest.agenda.generator.PrepOpsGenerator;
import com.theplatform.dfh.cp.endpoint.validation.TransformValidator;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.ByTitle;
import com.theplatform.dfh.endpoint.client.ObjectClient;
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
    private JsonHelper jsonHelper = new JsonHelper();

    private PrepOpsGenerator prepOpsGenerator;
    private ObjectClient<AgendaProgress> agendaProgressClient;
    private ObjectClient<Agenda> agendaClient;
    private ObjectClient<AgendaTemplate> agendaTemplateClient;
    private AgendaTemplateMapperFactory agendaTemplateMapperFactory;

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
        prepOpsGenerator = new PrepOpsGenerator();
        agendaProgressClient = new DataObjectRequestProcessorClient<>(new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister));
        agendaTemplateClient = new DataObjectRequestProcessorClient<>(new AgendaTemplateRequestProcessor(agendaTemplatePersister));
        agendaClient = new DataObjectRequestProcessorClient<>(new AgendaRequestProcessor(
            agendaPersister,
            agendaProgressPersister,
            readyAgendaPersister,
            operationProgressPersister,
            insightPersister,
            customerPersister
        ));
        agendaTemplateMapperFactory = new AgendaTemplateMapperFactory();
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
        ObjectTracker<AgendaProgress> agendaProgressTracker = trackerManager.register(new EndpointObjectTracker<>(agendaProgressClient, AgendaProgress.class));

        ////
        // persist the prep/exec progress
        ////
        DataObjectResponse<AgendaProgress> prepAgendaProgressResponse = createAgendaProgress(
            transformRequest.getLinkId(), transformRequest.getExternalId(), transformRequest.getCustomerId(), transformRequest.getCid());
        if (prepAgendaProgressResponse.isError())
        {
            deleteTransformRequest(transformRequest.getId());
            return new DefaultDataObjectResponse<>(prepAgendaProgressResponse.getErrorResponse());
        }
        AgendaProgress prepAgendaProgress = prepAgendaProgressResponse.getFirst();
        agendaProgressTracker.registerObject(prepAgendaProgress.getId());

        DataObjectResponse<AgendaProgress> execAgendaProgressResponse = createAgendaProgress(
            transformRequest.getLinkId(), transformRequest.getExternalId(), transformRequest.getCustomerId(), transformRequest.getCid());
        if (execAgendaProgressResponse.isError())
        {
            deleteTransformRequest(transformRequest.getId());
            trackerManager.cleanUp();
            return new DefaultDataObjectResponse<>(execAgendaProgressResponse.getErrorResponse());
        }
        AgendaProgress execAgendaProgress = execAgendaProgressResponse.getFirst();
        agendaProgressTracker.registerObject(execAgendaProgress.getId());

        if(transformRequest.getParams() == null) transformRequest.setParams(new ParamsMap());
        //This information is used for prep agenda generation, maybe these don't belong on the transform request itself
        transformRequest.getParams().put(GeneralParamKey.progressId, prepAgendaProgress.getId());
        transformRequest.getParams().put(GeneralParamKey.execProgressId, execAgendaProgress.getId());

        DataObjectResponse<Agenda> agendaResponse = createAgenda(transformRequest, prepAgendaProgressResponse.getFirst(), request.getCID());
        if (agendaResponse.isError())
        {
            deleteTransformRequest(transformRequest.getId());
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
            deleteTransformRequest(transformRequest.getId());
            trackerManager.cleanUp();
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(e, 500, request.getCID()));
        }

        return response;
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

    private DataObjectResponse<Agenda> createAgenda(TransformRequest transformRequest, AgendaProgress prepAgendaProgress, String cid)
    {
        ////
        // persist the prepAgenda (this is intentionally last as the Agenda may begin processing immediately)
        ////
        Agenda prepAgenda = generateTemplatedAgenda(transformRequest);
        if(prepAgenda == null)
        {
            prepAgenda = prepOpsGenerator.generateAgenda(transformRequest, prepAgendaProgress.getId());
        }
        else
        {
            prepAgenda.setCustomerId(transformRequest.getCustomerId());
            // set the progress id on the agenda
            if(prepAgenda.getParams() == null) prepAgenda.setParams(new ParamsMap());
            addParamsFromTransformRequest(prepAgenda, transformRequest);
            prepAgenda.setJobId(transformRequest.getId());
            prepAgenda.setLinkId(transformRequest.getLinkId());
            prepAgenda.setCid(transformRequest.getCid());
            prepAgenda.setCustomerId(transformRequest.getCustomerId());
            if (!StringUtils.isBlank(prepAgendaProgress.getId()))
                prepAgenda.setProgressId(prepAgendaProgress.getId());
        }

        DataObjectResponse<Agenda> prepAgendaResponse = null;
        try
        {
            prepAgendaResponse = agendaClient.persistObject(prepAgenda);
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
            return agendaProgressClient.persistObject(agendaProgress);
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

    /**
     * Adds any params to the Agenda from the TransformRequest
     * @param agenda The agenda to add any necessary params to
     * @param transformRequest The TransformRequest to pull information from
     */
    protected void addParamsFromTransformRequest(Agenda agenda, TransformRequest transformRequest)
    {

        if(agenda.getParams() == null) agenda.setParams(new ParamsMap());

        if(!StringUtils.isBlank(transformRequest.getExternalId())) agenda.getParams().put(GeneralParamKey.externalId, transformRequest.getExternalId());

        ParamsMap transformParams = transformRequest.getParams();
        if (transformParams != null && transformParams.containsKey(GeneralParamKey.doNotRun))
            agenda.getParams().put(GeneralParamKey.doNotRun, transformParams.get(GeneralParamKey.doNotRun));

        agenda.setCid(transformRequest.getCid());
    }

    private Agenda generateTemplatedAgenda(TransformRequest transformRequest)
    {
        DataObjectResponse<AgendaTemplate> response = null;
        if(transformRequest.getAgendaTemplateId() != null)
            response = agendaTemplateClient.getObject(transformRequest.getAgendaTemplateId());
        else if(transformRequest.getAgendaTemplateTitle() != null)
            response = agendaTemplateClient.getObjects(Collections.singletonList(new ByTitle(transformRequest.getAgendaTemplateTitle())));

        if(response == null)
        {
            logger.info("No agendaTemplateId or agendaTemplateTitle provided on TransformRequest.");
            return null;
        }

        if(!response.isError() && response.getFirst() != null)
        {
            AgendaTemplate agendaTemplate = response.getFirst();
            Agenda generatedAgenda = agendaTemplateMapperFactory
                .createAgendaTemplateMapper()
                .map(agendaTemplate, jsonHelper.getObjectMapper().valueToTree(Collections.singletonMap("fission.transformRequest", transformRequest)));
            logger.info("Generated Agenda: {}", jsonHelper.getJSONString(generatedAgenda));
            return generatedAgenda;
        }
        else
        {
            logger.error("No AgendaTemplate could be found by id: [{}] or title: [{}]", transformRequest.getAgendaTemplateId(), transformRequest.getAgendaTemplateTitle());
        }
        return null;
    }

    public void setPrepOpsGenerator(PrepOpsGenerator prepOpsGenerator)
    {
        this.prepOpsGenerator = prepOpsGenerator;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setAgendaProgressClient(ObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    public void setAgendaClient(ObjectClient<Agenda> agendaClient)
    {
        this.agendaClient = agendaClient;
    }

    public void setAgendaTemplateClient(ObjectClient<AgendaTemplate> agendaTemplateClient)
    {
        this.agendaTemplateClient = agendaTemplateClient;
    }

    public void setAgendaTemplateMapperFactory(AgendaTemplateMapperFactory agendaTemplateMapperFactory)
    {
        this.agendaTemplateMapperFactory = agendaTemplateMapperFactory;
    }

    private void deleteTransformRequest(String id)
    {
        try
        {
            objectPersister.delete(id);
        } catch (PersistenceException e)
        {
            logger.error("Failed to delete TransformRequest with id {}", id, e);
        }
    }
}
