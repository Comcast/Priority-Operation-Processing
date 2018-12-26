package com.theplatform.dfh.cp.endpoint.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.scheduling.agenda.insight.mapper.InsightSelector;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.query.scheduling.ByAgendaId;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Agenda specific RequestProcessor
 */
public class AgendaRequestProcessor extends BaseRequestProcessor<Agenda>
{
    private static final Logger logger = LoggerFactory.getLogger(AgendaRequestProcessor.class);
    private JsonHelper jsonHelper = new JsonHelper();

    private HttpCPObjectClient<AgendaProgress> agendaProgressClient;
    private HttpCPObjectClient<OperationProgress> operationProgressClient;
    private ObjectPersister<ReadyAgenda> readyAgendaObjectPersister;
    private InsightSelector insightSelector;

    public AgendaRequestProcessor(ObjectPersister<Agenda> agendaRequestObjectPersister,
        ObjectPersister<ReadyAgenda> readyAgendaObjectPersister,
        HttpURLConnectionFactory httpURLConnectionFactory,
        String agendaProgressURL,
        String operationProgressURL,
        String insightURL,
        String customerURL)
    {
        this(agendaRequestObjectPersister,
            readyAgendaObjectPersister,
            new HttpCPObjectClient<>(agendaProgressURL, httpURLConnectionFactory, AgendaProgress.class),
            new HttpCPObjectClient<>(operationProgressURL, httpURLConnectionFactory, OperationProgress.class),
            new InsightSelector(httpURLConnectionFactory, insightURL, customerURL));
    }

    AgendaRequestProcessor(ObjectPersister<Agenda> agendaRequestObjectPersister,
        ObjectPersister<ReadyAgenda> readyAgendaObjectPersister,
        HttpCPObjectClient<AgendaProgress> agendaProgressClient,
        HttpCPObjectClient<OperationProgress> operationProgressClient,
        InsightSelector insightSelector)
    {
        super(agendaRequestObjectPersister);
        this.readyAgendaObjectPersister = readyAgendaObjectPersister;
        this.agendaProgressClient = agendaProgressClient;
        this.operationProgressClient = operationProgressClient;
        this.insightSelector = insightSelector;
    }

    @Override
    public ObjectPersistResponse handlePOST(Agenda objectToPersist)
    {
        // verify that all Operations have different names
        if (objectToPersist.getOperations() != null) verifyUniqueOperationsName(objectToPersist.getOperations());

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

        String objectId = UUID.randomUUID().toString();
        objectToPersist.setId(objectId);
        try
        {
            objectPersister.persist(objectToPersist);
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
                readyAgenda.setId(UUID.randomUUID().toString());
                readyAgenda.setInsightId(insight.getId());
                readyAgenda.setAdded(new Date());
                readyAgenda.setAgendaId(objectId);
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
            logger.warn("No insight was found for new agenda: {}", objectId);
        }

        ObjectPersistResponse response = new ObjectPersistResponse(objectId);
        if(response.getParams() == null) response.setParams(new ParamsMap());
        response.getParams().put(GeneralParamKey.progressId, agendaProgressId);
        return response;
    }

    /**
     * DELETE an Agenda and it's 
     * @param id The id of the object to delete
     */
    public void handleDelete(String id)
    {
        try
        {
            objectPersister.delete(id);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException(String.format("Unable to delete object by id {}", id), e);
        }

        ByAgendaId byAgendaId = new ByAgendaId(id);
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
    }

    void verifyUniqueOperationsName(List<Operation> operations)
    {
        Set<String> opNames = new HashSet<>();
        for (Operation op : operations)
        {
            String opName = op.getName();
            if (opName == null || opName.isEmpty())
                throw new IllegalArgumentException("Operations must have names.");
            boolean unique = opNames.add(op.getName().toLowerCase());
            if (!unique)
                throw new IllegalArgumentException("Operation names must be unique.");
        }
    }

    String persistAgendaProgress(Agenda agenda)
    {
        ////
        // persist the progress
        ////
        ObjectPersistResponse agendaProgressResponse;
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
            agendaProgressResponse = agendaProgressClient.persistObject(agendaProgress);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create the Progress generated from the Agenda.", e);
        }

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
}
