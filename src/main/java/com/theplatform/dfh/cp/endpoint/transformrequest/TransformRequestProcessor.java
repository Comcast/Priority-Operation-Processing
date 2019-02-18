package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.cleanup.EndpointObjectTracker;
import com.theplatform.dfh.cp.endpoint.cleanup.EndpointObjectTrackerManager;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.transformrequest.agenda.generator.PrepOpsGenerator;
import com.theplatform.dfh.cp.endpoint.validation.TransformValidator;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TransformRequest specific RequestProcessor
 */
public class TransformRequestProcessor extends DataObjectRequestProcessor<TransformRequest>
{
    private static final Logger logger = LoggerFactory.getLogger(TransformRequestProcessor.class);
    private JsonHelper jsonHelper = new JsonHelper();

    private PrepOpsGenerator prepOpsGenerator;
    private ObjectClient<AgendaProgress> agendaProgressClient;
    private ObjectClient<Agenda> agendaClient;

    public TransformRequestProcessor(
        ObjectPersister<TransformRequest> transformRequestObjectPersister,
        ObjectPersister<Agenda> agendaPersister,
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        super(transformRequestObjectPersister, new TransformValidator());
        prepOpsGenerator = new PrepOpsGenerator();
        agendaProgressClient = new DataObjectRequestProcessorClient<>(new AgendaProgressRequestProcessor(agendaProgressPersister, operationProgressPersister));
        agendaClient = new DataObjectRequestProcessorClient<>(new AgendaRequestProcessor(
            agendaPersister,
            agendaProgressPersister,
            readyAgendaPersister,
            operationProgressPersister,
            insightPersister,
            customerPersister
        ));
    }

    @Override
    public DataObjectResponse<TransformRequest> handlePOST(DataObjectRequest<TransformRequest> request)
    {
        //We create the transform req first, so we have the ID for progress operations.
        //If progress fails, we need to rollback the transformReq.
        DataObjectResponse<TransformRequest> response = super.handlePOST(request);
        if (response.isError())
            return response;
        TransformRequest transformRequest = response.getFirst();

        EndpointObjectTrackerManager trackerManager = new EndpointObjectTrackerManager();
        EndpointObjectTracker<AgendaProgress> agendaProgressTracker = new EndpointObjectTracker<>(agendaProgressClient);
        trackerManager.register(agendaProgressTracker);

        ////
        // persist the prep/exec progress
        ////
        DataObjectResponse<AgendaProgress> prepAgendaProgressResponse = createAgendaProgress(
            transformRequest.getId(), transformRequest.getExternalId(), transformRequest.getCustomerId(), request.getCID());
        if (prepAgendaProgressResponse.isError())
        {
            deleteTransformRequest(transformRequest.getId());
            return new DefaultDataObjectResponse<>(prepAgendaProgressResponse.getErrorResponse());
        }
        AgendaProgress prepAgendaProgress = prepAgendaProgressResponse.getFirst();
        agendaProgressTracker.registerObject(prepAgendaProgress.getId());

        DataObjectResponse<AgendaProgress> execAgendaProgressResponse = createAgendaProgress(
            transformRequest.getId(), transformRequest.getExternalId(), transformRequest.getCustomerId(), request.getCID());
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

        transformRequest.getParams().put(GeneralParamKey.agendaId, agendaResponse.getFirst().getId());
        return response;
    }

    private DataObjectResponse<Agenda> createAgenda(TransformRequest request, AgendaProgress prepAgendaProgressResponse, String cid)
    {
        ////
        // persist the prepAgenda (this is intentionally last as the Agenda may begin processing immediately)
        ////
        Agenda prepAgenda = prepOpsGenerator.generateAgenda(request, prepAgendaProgressResponse.getId());

        //logger.debug("Generated Agenda: {}", jsonHelper.getJSONString(agenda));
        DataObjectResponse<Agenda> prepAgendaResponse = new DefaultDataObjectResponse<>();
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
