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
import com.theplatform.dfh.cp.scheduling.agenda.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.ValidationException;
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
        super(agendaRequestObjectPersister);
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
        //@todo validation should be a NotFoundException??
        if(insight == null)
            throw new ValidationException(String.format("No available insights for processing agenda %s", objectToPersist.getId()));

        ParamsMap paramsMap = objectToPersist.getParams();
        paramsMap = paramsMap == null ? new ParamsMap() : paramsMap;

        String agendaProgressId = objectToPersist.getProgressId();
        if (agendaProgressId == null)
        {
            agendaProgressId = persistAgendaProgress(objectToPersist);
            logger.info("Generated new progress: {}", agendaProgressId);
            objectToPersist.setProgressId(agendaProgressId);
            objectToPersist.setParams(paramsMap);
        }
        else
        {
            logger.info("Using existing progress: {}", agendaProgressId);
        }

        if (objectToPersist.getOperations() != null)
            persistOperationProgresses(objectToPersist, agendaProgressId);

        String agendaId = null;
        try
        {
            Agenda persistedAgenda = objectPersister.persist(objectToPersist);
            agendaId = persistedAgenda.getId();
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException("Unable to create object", e);
        }

        if(insight != null)
        {
            try
            {
                ReadyAgenda readyAgenda = new ReadyAgenda();
                readyAgenda.setInsightId(insight.getId());
                readyAgenda.setAdded(new Date());
                readyAgenda.setAgendaId(agendaId);
                readyAgenda.setCustomerId(objectToPersist.getCustomerId());
                readyAgendaObjectPersister.persist(readyAgenda);
            }
            catch (PersistenceException e)
            {
                throw new BadRequestException("Unable to create ReadyAgenda", e);
            }
        }
        else
        {
            logger.warn("No insight was found for new agenda: {}", agendaId);
        }

        Agenda response = new Agenda();
        response.setId(agendaId);
        if(response.getParams() == null) response.setParams(new ParamsMap());
        response.getParams().put(GeneralParamKey.progressId, agendaProgressId);
        DataObjectResponse<Agenda> dataObjectResponse = new DefaultDataObjectResponse<>();
        dataObjectResponse.add(response);
        return dataObjectResponse;
    }

    @Override
    public DataObjectResponse<Agenda> handleDELETE(DataObjectRequest<Agenda> request)
    {
        try
        {
            objectPersister.delete(request.getId());
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to delete object by id: %1$s", request.getId()), e);
        }

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
        return new DefaultDataObjectResponse<>();
    }

    String persistAgendaProgress(Agenda agenda)
    {
        ////
        // persist the progress
        ////
        AgendaProgress agendaProgressResponse;
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setLinkId(agenda.getLinkId());
        agendaProgress.setProcessingState(ProcessingState.WAITING);
        agendaProgress.setAddedTime(new Date());
        if(agenda.getParams() != null)
        {
            ParamsMap paramsMap = agenda.getParams();
            String externalId = paramsMap.getString(GeneralParamKey.externalId);
            if(!StringUtils.isBlank(externalId)) agendaProgress.setExternalId(externalId);
        }
        logger.debug("Generated AgendaProgress: {}", jsonHelper.getJSONString(agendaProgress));
        try
        {
            DataObjectResponse<AgendaProgress> dataObjectResponse = agendaProgressClient.persistObject(agendaProgress);
            if(dataObjectResponse.isError()) throw dataObjectResponse.getException();
            agendaProgressResponse = dataObjectResponse.getFirst();
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create the Progress generated from the Agenda.", e);
        }

        if(agendaProgressResponse == null)
            throw new RuntimeException("AgendaProgress persistence failed.");

        return agendaProgressResponse.getId();
    }

    void persistOperationProgresses(Agenda agenda, String agendaProgressId)
    {
        ////
        // persist operation progress
        ////
        for (Operation operation : agenda.getOperations())
        {
            OperationProgress operationProgress = new OperationProgress();
            operationProgress.setAgendaProgressId(agendaProgressId);
            operationProgress.setProcessingState(ProcessingState.WAITING);
            operationProgress.setOperation(operation.getName());
            operationProgress.setId(OperationProgress.generateId(agendaProgressId, operation.getName()));
            try {
                operationProgressClient.persistObject(operationProgress);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to create the OperationProgress generated from the TransformRequest.", e);
            }
        }
    }

    @Override
    public RequestValidator<DataObjectRequest<Agenda>> getRequestValidator()
    {
        return new AgendaValidator();
    }
}
