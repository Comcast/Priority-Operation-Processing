package com.theplatform.dfh.cp.endpoint.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Customer;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.CustomerRequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.InsightRequestProcessor;
import com.theplatform.dfh.cp.endpoint.operationprogress.OperationProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.validation.AgendaValidator;
import com.theplatform.dfh.cp.endpoint.facility.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.DataObjectErrorResponses;
import com.theplatform.dfh.endpoint.api.ErrorResponse;
import com.theplatform.dfh.endpoint.api.ObjectNotFoundException;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.scheduling.ByAgendaId;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.Date;

/**
 * Agenda specific RequestProcessor
 */
public class AgendaRequestProcessor extends DataObjectRequestProcessor<Agenda>
{
    private static final Logger logger = LoggerFactory.getLogger(AgendaRequestProcessor.class);
    private JsonHelper jsonHelper = new JsonHelper();

    private ObjectClient<AgendaProgress> agendaProgressClient;
    private ObjectClient<OperationProgress> operationProgressClient;
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
            new DataObjectRequestProcessorClient<>(new AgendaProgressRequestProcessor(agendaProgressPersister, operationProgressPersister)),
            new DataObjectRequestProcessorClient<>(new OperationProgressRequestProcessor(operationProgressPersister)),
            new InsightSelector(
                new DataObjectRequestProcessorClient<>(new InsightRequestProcessor(insightPersister)),
                new DataObjectRequestProcessorClient<>(new CustomerRequestProcessor(customerPersister))
            )
        );
    }

    AgendaRequestProcessor(ObjectPersister<Agenda> agendaRequestObjectPersister,
        ObjectPersister<ReadyAgenda> readyAgendaObjectPersister,
        ObjectClient<AgendaProgress> agendaProgressClient,
        ObjectClient<OperationProgress> operationProgressClient,
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
        Agenda objectToPersist = request.getDataObject();

        // verify we have a valid insight for this agenda
        Insight insight = insightSelector.select(objectToPersist);
        if(insight == null)
        {
            return new DefaultDataObjectResponse<>(DataObjectErrorResponses.objectNotFound(
                String.format("No available insights for processing agenda %s", objectToPersist.getId()),
                request.getCID()));
        }

        String agendaProgressId = objectToPersist.getProgressId();
        if (agendaProgressId == null)
        {
            DataObjectResponse<AgendaProgress> persistResponse = persistAgendaProgress(objectToPersist, request.getCID());
            if (persistResponse.isError())
            {
                return new DefaultDataObjectResponse<>(persistResponse.getErrorResponse());
            }
            agendaProgressId = persistResponse.getFirst().getId();
            objectToPersist.setProgressId(agendaProgressId);
        }
        else
        {
            logger.info("Using existing progress: {}", agendaProgressId);
        }

        if (objectToPersist.getOperations() != null)
        {
            DataObjectResponse<OperationProgress> persistResponse = persistOperationProgresses(objectToPersist, agendaProgressId, request.getCID());
            if (persistResponse.isError())
            {
                return new DefaultDataObjectResponse<>(persistResponse.getErrorResponse());
            }
        }

        DataObjectResponse<Agenda> response = super.handlePOST(request);
        Agenda agendaResp = response.getFirst();
        DataObjectResponse<ReadyAgenda> readyAgendaResponse = persistReadyAgenda(insight.getId(), agendaResp.getId(), agendaResp.getCustomerId(), request.getCID());
        if (readyAgendaResponse.isError())
        {
            return new DefaultDataObjectResponse<>(readyAgendaResponse.getErrorResponse());
        }

        return response;
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
            logger.error("Unable to delete ReadyAgenda.", e);
        }
        return agendaResp;
    }

    DataObjectResponse<ReadyAgenda> persistReadyAgenda(String insightId, String agendaId, String customerId, String cid)
    {
        try
        {
            ReadyAgenda readyAgenda = new ReadyAgenda();
            readyAgenda.setInsightId(insightId);
            readyAgenda.setAdded(new Date());
            readyAgenda.setAgendaId(agendaId);
            readyAgenda.setCustomerId(customerId);
            readyAgendaObjectPersister.persist(readyAgenda);
            return new DefaultDataObjectResponse<>();
        }
        catch (PersistenceException e)
        {
            return new DefaultDataObjectResponse<>(DataObjectErrorResponses.badRequest(new BadRequestException("Unable to create ReadyAgenda", e), cid));
        }
    }

    DataObjectResponse<AgendaProgress> persistAgendaProgress(Agenda agenda, String cid)
    {
        ////
        // persist the progress
        ////
        AgendaProgress agendaProgressResponse;
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setCustomerId(agenda.getCustomerId());
        agendaProgress.setLinkId(agenda.getLinkId());
        agendaProgress.setProcessingState(ProcessingState.WAITING);
        agendaProgress.setAddedTime(new Date());

        ParamsMap paramsMap = agenda.getParams() == null ? new ParamsMap() : agenda.getParams();
        if(paramsMap == null)
            agenda.setParams(new ParamsMap());

        String externalId = paramsMap.getString(GeneralParamKey.externalId);
        if(!StringUtils.isBlank(externalId)) agendaProgress.setExternalId(externalId);

        logger.debug("Generated AgendaProgress: {}", jsonHelper.getJSONString(agendaProgress));
        DataObjectResponse<AgendaProgress> dataObjectResponse = agendaProgressClient.persistObject(agendaProgress);
        if(dataObjectResponse.isError()) return dataObjectResponse;
        agendaProgressResponse = dataObjectResponse.getFirst();

        if(agendaProgressResponse == null)
        {
            dataObjectResponse.setErrorResponse(DataObjectErrorResponses.buildErrorResponse(new RuntimeException("AgendaProgress persistence failed."), 400, cid));
            return dataObjectResponse;
        }

        logger.info("Generated new progress: {}", agendaProgressResponse.getId());
        return dataObjectResponse;
    }

    DataObjectResponse<OperationProgress> persistOperationProgresses(Agenda agenda, String agendaProgressId, String cid)
    {
        ////
        // persist operation progress
        ////
        DataObjectResponse<OperationProgress> dataObjectResponse = new DefaultDataObjectResponse<>();
        for (Operation operation : agenda.getOperations())
        {
            OperationProgress operationProgress = new OperationProgress();
            operationProgress.setCustomerId(agenda.getCustomerId());
            operationProgress.setAgendaProgressId(agendaProgressId);
            operationProgress.setProcessingState(ProcessingState.WAITING);
            operationProgress.setOperation(operation.getName());
            operationProgress.setId(OperationProgress.generateId(agendaProgressId, operation.getName()));
            try {
                dataObjectResponse.add(operationProgressClient.persistObject(operationProgress).getFirst());
            }
            catch (Exception e)
            {
                dataObjectResponse.setErrorResponse(DataObjectErrorResponses.buildErrorResponse(new RuntimeException("Failed to create the OperationProgress generated from the " +
                    "TransformRequest.", e), 400, cid));
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
}
