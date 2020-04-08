package com.comcast.pop.cp.endpoint.agenda;

import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaInsight;
import com.comcast.pop.api.facility.Customer;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.AgendaProgressBuilder;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.base.AbstractServiceRequestProcessor;
import com.comcast.pop.endpoint.base.DataObjectRequestProcessor;
import com.comcast.pop.endpoint.base.EndpointDataObjectRequestProcessor;
import com.comcast.pop.endpoint.base.validation.RequestValidator;
import com.comcast.pop.cp.endpoint.cleanup.EndpointObjectTracker;
import com.comcast.pop.cp.endpoint.cleanup.ObjectTrackerManager;
import com.comcast.pop.cp.endpoint.cleanup.PersisterObjectTracker;
import com.comcast.pop.cp.endpoint.data.EndpointObjectGenerator;
import com.comcast.pop.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.comcast.pop.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.pop.cp.endpoint.validation.AgendaValidator;
import com.comcast.pop.cp.endpoint.resourcepool.insight.mapper.InsightSelector;
import com.comcast.pop.scheduling.api.ReadyAgenda;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.endpoint.api.data.DataObjectRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.endpoint.api.data.query.scheduling.ByAgendaId;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

/**
 * Agenda specific RequestProcessor
 */
public class AgendaRequestProcessor extends EndpointDataObjectRequestProcessor<Agenda>
{
    private static final Logger logger = LoggerFactory.getLogger(AgendaRequestProcessor.class);
    private JsonHelper jsonHelper = new JsonHelper();

    private DataObjectRequestProcessor<AgendaProgress> agendaProgressClient;
    private DataObjectRequestProcessor<OperationProgress> operationProgressClient;
    private ObjectPersister<ReadyAgenda> readyAgendaObjectPersister;
    private InsightSelector insightSelector;

    public AgendaRequestProcessor(ObjectPersister<Agenda> agendaRequestPersister,
        ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<ReadyAgenda> readyAgendaPersister,
        ObjectPersister<OperationProgress> operationProgressPersister,
        ObjectPersister<Insight> insightPersister,
        ObjectPersister<Customer> customerPersister)
    {
        this(agendaRequestPersister,
            readyAgendaPersister,
            new AgendaProgressRequestProcessor(agendaProgressPersister, agendaRequestPersister, operationProgressPersister),
            new OperationProgressRequestProcessor(operationProgressPersister),
            new InsightSelector(insightPersister,customerPersister)
        );
    }

    public AgendaRequestProcessor(ObjectPersister<Agenda> agendaRequestObjectPersister,
        ObjectPersister<ReadyAgenda> readyAgendaObjectPersister,
        DataObjectRequestProcessor<AgendaProgress> agendaProgressClient,
        DataObjectRequestProcessor<OperationProgress> operationProgressClient,
        InsightSelector insightSelector)
    {
        super(agendaRequestObjectPersister, new AgendaValidator());
        this.readyAgendaObjectPersister = readyAgendaObjectPersister;
        this.agendaProgressClient = agendaProgressClient;
        this.operationProgressClient = operationProgressClient;
        this.insightSelector = insightSelector;
    }

    @Override
    public DataObjectResponse<Agenda> handlePOST(DataObjectRequest<Agenda> request)
    {
        //we call super below, but no point if we fail validation, so let's validate here.
        if(getRequestValidator() != null) getRequestValidator().validatePOST(request);

        Agenda agendaToPersist = request.getDataObject();
        final String customerID = agendaToPersist.getCustomerId();

        ObjectTrackerManager trackerManager = new ObjectTrackerManager();
        trackerManager.register(new EndpointObjectTracker<>(agendaProgressClient, AgendaProgress.class, customerID));
        trackerManager.register(new EndpointObjectTracker<>(operationProgressClient, OperationProgress.class, customerID));
        trackerManager.register(new PersisterObjectTracker<>(getObjectPersister(), Agenda.class));

        // verify we have a valid insight for this agenda
        Insight insight = null;
        try
        {
            insight = getInsightSelector().select(agendaToPersist);
        } catch (ValidationException e)
        {
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(e, e.getResponseCode(), request.getCID()));
        }
        if(insight == null)
        {
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.objectNotFound(
                "No available insights for processing agenda",
                request.getCID()));
        }

        // The Agenda id is generated up front for use on other object updates/creates
        agendaToPersist.setId(UUID.randomUUID().toString());
        AgendaInsight agendaInsight = new AgendaInsight();
        agendaInsight.setInsightId(insight.getId());
        agendaInsight.setResourcePoolId(insight.getResourcePoolId());
        agendaToPersist.setCid(agendaToPersist.getCid() == null ? UUID.randomUUID().toString() : agendaToPersist.getCid());
        agendaToPersist.setAgendaInsight(agendaInsight);
        agendaToPersist.setParams(agendaToPersist.getParams() == null ? new ParamsMap() : agendaToPersist.getParams());

        DataObjectResponse<AgendaProgress> progressResponse =
            agendaToPersist.getProgressId() == null ? postAgendaProgress(agendaToPersist, request.getCID()) : putAgendaProgress(agendaToPersist);
        if (progressResponse.isError())
        {
            trackerManager.cleanUp();
            return new DefaultDataObjectResponse<>(progressResponse.getErrorResponse());
        }
        final String agendaProgressId = progressResponse.getFirst().getId();
        trackerManager.track(progressResponse.getFirst());
        agendaToPersist.setProgressId(agendaProgressId);

        // always create new OperationProgress objects
        if (agendaToPersist.getOperations() != null)
        {
            DataObjectResponse<OperationProgress> persistResponse = persistOperationProgresses(agendaToPersist, request.getCID(), trackerManager);
            if (persistResponse.isError())
            {
                trackerManager.cleanUp();
                return new DefaultDataObjectResponse<>(persistResponse.getErrorResponse());
            }
        }

        DataObjectResponse<Agenda> agendaPersistResponse = super.handlePOST(request);
        if (agendaPersistResponse.isError())
        {
            trackerManager.cleanUp();
            return agendaPersistResponse;
        }

        Agenda agendaResp = agendaPersistResponse.getFirst();
        trackerManager.track(agendaResp);

        if (!(agendaToPersist.getParams().containsKey(GeneralParamKey.doNotRun)))
        {
            DataObjectResponse<ReadyAgenda> readyAgendaResponse = persistReadyAgenda(insight.getId(), agendaResp.getId(), agendaResp.getCustomerId(), request.getCID());
            if (readyAgendaResponse.isError())
            {
                trackerManager.cleanUp();
                return new DefaultDataObjectResponse<>(readyAgendaResponse.getErrorResponse());
            }
        }

        return agendaPersistResponse;
    }

    @Override
    public DataObjectResponse<Agenda> handlePUT(DataObjectRequest<Agenda> request)
    {
        // NOTE: this is currently only intended for use with the Agenda expansion functionality, no agenda/operation progress is affected as with the other methods
        return super.handlePUT(request);
    }

    @Override
    public DataObjectResponse<Agenda> handleDELETE(DataObjectRequest<Agenda> request)
    {
        DataObjectResponse<Agenda> agendaResp = super.handleDELETE(request);
        ByAgendaId byAgendaId = new ByAgendaId(request.getId());
        try
        {
            DataObjectFeed<ReadyAgenda> dataObjectFeed = readyAgendaObjectPersister.retrieve(Collections.singletonList(byAgendaId));
            for (ReadyAgenda readyAgenda : dataObjectFeed.getAll())
            {
                readyAgendaObjectPersister.delete(readyAgenda.getId());
            }
        }
        catch (PersistenceException e)
        {
            RuntimeException runtimeException = new RuntimeException("Error persisting Agenda " + request.getId(), e);
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(runtimeException, 400, request.getCID()));
        }
        return agendaResp;
    }

    private DataObjectResponse<ReadyAgenda> persistReadyAgenda(String insightId, String agendaId, String customerId, String cid)
    {
        try
        {
            ReadyAgenda readyAgenda = new ReadyAgenda();
            readyAgenda.setInsightId(insightId);
            readyAgenda.setAdded(new Date());
            readyAgenda.setAgendaId(agendaId);
            readyAgenda.setCustomerId(customerId);
            ReadyAgenda created = readyAgendaObjectPersister.persist(readyAgenda);
            DataObjectResponse<ReadyAgenda> response = new DefaultDataObjectResponse<>();
            return response;
        }
        catch (PersistenceException e)
        {
            RuntimeException runtimeException = new RuntimeException("Error persisting ReadyAgenda for Agenda " + agendaId, e);
            return new DefaultDataObjectResponse<>(ErrorResponseFactory.buildErrorResponse(runtimeException, 400, cid));
        }
    }

    private DataObjectResponse<AgendaProgress> postAgendaProgress(Agenda agenda, String cid)
    {
        ////
        // persist the progress
        ////
        AgendaProgress agendaProgress = new AgendaProgressBuilder()
            .withAgendaFields(agenda)
            .withProcessingState(ProcessingState.WAITING)
            .build();

        // HACK / TODO: ExternalId should be a field on the Agenda and copied to the AgendaProgress
        if(agenda.getParams() != null && agenda.getParams().containsKey(GeneralParamKey.externalId))
        {
            agendaProgress.setExternalId(agenda.getParams().getString(GeneralParamKey.externalId));
        }

        logger.debug("Generated AgendaProgress: {}", jsonHelper.getJSONString(agendaProgress));

        //If the customer can create transform requests then they are allowed to create progress.
        //If we don't use a service user authentication then we have to grant all customers write access to progress. No!
        DataObjectRequest<AgendaProgress> request = DefaultDataObjectRequest.serviceUserAuthInstance(agendaProgress);
        DataObjectResponse<AgendaProgress> dataObjectResponse = agendaProgressClient.handlePOST(request);
        AbstractServiceRequestProcessor.addErrorForObjectNotFound(dataObjectResponse, AgendaProgress.class, null, cid);

        return dataObjectResponse;
    }

    private DataObjectResponse<AgendaProgress> putAgendaProgress(Agenda agenda)
    {
        logger.info("Using existing progress: {} Updated associated agendaId: {}", agenda.getProgressId(), agenda.getId());
        AgendaProgress agendaProgress = new AgendaProgressBuilder()
            .withAgendaFields(agenda)
            .build();
        // NOTE: on failure the AgendaProgress with have an invalid agendaId -- this is harmless
        return agendaProgressClient.handlePUT(DefaultDataObjectRequest.customerAuthInstance(agenda.getCustomerId(),agendaProgress));
    }

    private DataObjectResponse<OperationProgress> persistOperationProgresses(Agenda agenda, String cid, ObjectTrackerManager trackerManager)
    {
        ////
        // persist operation progress
        ////
        DataObjectResponse<OperationProgress> dataObjectResponse = new DefaultDataObjectResponse<>();
        for (Operation operation : agenda.getOperations())
        {
            OperationProgress operationProgress = EndpointObjectGenerator.generateWaitingOperationProgress(agenda, operation);

            try {
                //If the customer can create transform requests then they are allowed to create progress.
                //If we don't use a service user authentication then we have to grant all customers write access to progress. No!
                DataObjectRequest<OperationProgress> request = DefaultDataObjectRequest.serviceUserAuthInstance(operationProgress);
                DataObjectResponse<OperationProgress> opProgressResponse = operationProgressClient.handlePOST(request);

                if (opProgressResponse == null || opProgressResponse.isError())
                    return opProgressResponse;
                OperationProgress created = opProgressResponse.getFirst();
                dataObjectResponse.add(created);
                trackerManager.track(created);
            }
            catch (Exception e)
            {
                dataObjectResponse.setErrorResponse(ErrorResponseFactory.buildErrorResponse(new RuntimeException("Failed to create the OperationProgress generated from the " +
                    "Agenda.", e), 400, cid));
                return dataObjectResponse;
            }
        }
        return dataObjectResponse;
    }

    @Override
    public RequestValidator<DataObjectRequest<Agenda>> getRequestValidator()
    {
        return new AgendaValidator();
    }

    public void setInsightSelector(InsightSelector insightSelector)
    {
        this.insightSelector = insightSelector;
    }

    public InsightSelector getInsightSelector()
    {
        return insightSelector;
    }

    public void setAgendaProgressClient(DataObjectRequestProcessor<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    public void setOperationProgressClient(DataObjectRequestProcessor<OperationProgress> operationProgressClient)
    {
        this.operationProgressClient = operationProgressClient;
    }

    public void setReadyAgendaObjectPersister(ObjectPersister<ReadyAgenda> readyAgendaObjectPersister)
    {
        this.readyAgendaObjectPersister = readyAgendaObjectPersister;
    }

    private void deleteAgenda(String id)
    {
        try
        {
            objectPersister.delete(id);
        } catch (PersistenceException e)
        {
            logger.error("Failed to delete Agenda with id {}", id, e);
        }
    }
}
