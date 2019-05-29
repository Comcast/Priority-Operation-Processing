package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.Report;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.AgendaReporter;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.AgendaReports;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.AgendaResponseReporter;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.validation.AgendaServiceValidator;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static com.theplatform.dfh.cp.endpoint.agenda.reporter.AgendaResponseReporter.AGENDA_RESPONSE_REPORTER_KEY;

/**
 * Agenda service request processor
 */
public class AgendaServiceRequestProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(AgendaServiceRequestProcessor.class);
    private static final String AGENDA_PREFIX = "Agenda metadata - ";
    private static final String AGENDA_REQUEST_TEMPLATE = "Agenda Request metadata - InsightId: %s; count: %d.";

    private static final AgendaReports[] AGENDA_REPORTS = {
            AgendaReports.CID,
            AgendaReports.AGENDA_ID,
            AgendaReports.LINK_ID,
            AgendaReports.CUSTOMER_ID,
            AgendaReports.AGENDA_STATUS,
            AgendaReports.MILLISECONDS_IN_QUEUE,
            AgendaReports.AGENDA_TYPE,
            AgendaReports.OPERATION_PAYLOAD
    };

    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;



    public AgendaServiceRequestProcessor(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory, ObjectPersister<Insight> insightPersister,
                                         ObjectPersister<Agenda> agendaPersister, ObjectPersister<AgendaProgress> agendaProgressPersister)
    {
        this(agendaInfoItemQueueFactory, insightPersister, agendaPersister);
        this.agendaProgressPersister = agendaProgressPersister;
    }


    protected AgendaServiceRequestProcessor(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
        this.insightPersister = insightPersister;
        this.agendaPersister = agendaPersister;
    }

    public GetAgendaResponse processRequest(ServiceRequest<GetAgendaRequest> serviceRequest)
    {
        try
        {
            getRequestValidator().validateGET(serviceRequest);
        } catch (ValidationException e)
        {
            String cid = null;
            if (serviceRequest != null)
                cid = serviceRequest.getCID();
            return new GetAgendaResponse(ErrorResponseFactory.buildErrorResponse(e, e.getResponseCode(), cid));
        }

        GetAgendaRequest getAgendaRequest = serviceRequest.getPayload();
        if (getAgendaRequest.getInsightId() == null)
        {
            return new GetAgendaResponse(ErrorResponseFactory.badRequest("No insight id provided.  Cannot process getAgenda request.", serviceRequest.getCID()));
        }

        logger.info(String.format(AGENDA_REQUEST_TEMPLATE, getAgendaRequest.getInsightId(), getAgendaRequest.getCount()));

        Insight insight;
        try
        {
            insight = insightPersister.retrieve(getAgendaRequest.getInsightId());
        }
        catch(PersistenceException e)
        {
            return new GetAgendaResponse(ErrorResponseFactory.buildErrorResponse(e, 400, serviceRequest.getCID()));
        }

        if(insight == null)
        {
            return new GetAgendaResponse(ErrorResponseFactory.objectNotFound(String.format("No insight found with id %s. Cannot process getAgenda request.",
                getAgendaRequest.getInsightId()), serviceRequest.getCID()));
        }

        try
        {
            QueueResult<AgendaInfo> agendaInfoQueueResult = pollInsightQueue(insight, getAgendaRequest.getCount());
            if(agendaInfoQueueResult.isSuccessful())
            {
                List<Agenda> agendaList = new LinkedList<>();
                logger.info("Insight {} queue: {} poll results: {}", insight.getId(), insight.getQueueName(),
                    agendaInfoQueueResult.getData() == null ? 0 : agendaInfoQueueResult.getData().size());
                // TODO: if the results include more than the desired amount cap it
                // TODO: this is not optimal, a multi-get would be better...
                if(agendaInfoQueueResult.getData() != null)
                {
                    for (AgendaInfo agendaInfo : agendaInfoQueueResult.getData())
                    {
                        Agenda agenda = retrieveAgenda(agendaInfo.getAgendaId());
                        if (agenda == null)
                        {
                            logger.warn("Could not find Agenda with id {}", agendaInfo.getAgendaId());
                        }  else
                        {
                            if(agenda.getParams() == null)
                            {
                                agenda.setParams(new ParamsMap());
                            }
                            if(agendaInfo.getAdded() == null)
                            {
                                logger.warn("No 'added' field on AgendaInfo for agenda: " + agendaInfo.getAgendaId() );
                            }
                            else
                            {
                                agenda.getParams().put(Report.ADDED_KEY, agendaInfo.getAdded());
                            }
                            agendaList.add(agenda);
                        }
                    }
                }
                return createAgendaServiceResult(agendaList);
            }
            else
            {
                return new GetAgendaResponse(ErrorResponseFactory.runtimeServiceException("Failed to poll queue for AgendaInfo.", serviceRequest.getCID()));
            }
        }
        catch(PersistenceException e)
        {
            return new GetAgendaResponse(ErrorResponseFactory.buildErrorResponse(e, 400, serviceRequest.getCID()));
        }
        catch(BadRequestException e)
        {
            return new GetAgendaResponse(ErrorResponseFactory.buildErrorResponse(e, e.getResponseCode(), serviceRequest.getCID()));
        }
    }

    private GetAgendaResponse createAgendaServiceResult(Collection<Agenda> agendas)
    {
        GetAgendaResponse getAgendaResponse = new GetAgendaResponse(agendas);
        if(agendas == null || agendas.isEmpty())
        {
            return getAgendaResponse;
        }
        AgendaResponseReporter agendaResponseReporter = new AgendaResponseReporter(getAgendaResponse, new AgendaReporter(AGENDA_PREFIX, AGENDA_REPORTS));
        Agenda agenda = agendas.iterator().next();
        try
        {
            AgendaProgress agendaProgress = agendaProgressPersister.retrieve(agenda.getProgressId());
            agendaProgress.getParams().put(AGENDA_RESPONSE_REPORTER_KEY, agendaResponseReporter);
            agendaProgressPersister.update(agendaProgress);
        } catch (Exception | PersistenceException e)
        {
            logger.error("Failed to update agendaProgress with agenda report details: " + agenda.getProgressId(), e);
        }
        return getAgendaResponse;
    }

    public Agenda retrieveAgenda(String agendaId) throws PersistenceException
    {
        return agendaPersister.retrieve(agendaId);
    }

    /**
     * Polls the queue associated with the specified insight for a number of results.
     * @param insight The insight to poll the queue for
     * @param maxResults The maximum number of results to take from the queue
     * @return A QueueResult
     */
    public QueueResult<AgendaInfo> pollInsightQueue(Insight insight, int maxResults) throws BadRequestException
    {
        if(maxResults < 1)
        {
            // todo do we want to add ErrorResponse to QueueResult?
            throw new BadRequestException(String.format("The maximum results specified is not supported: %1$s", maxResults));
        }
        // TODO: might be an insight id and we have an insight client
        ItemQueue<AgendaInfo> itemQueue = agendaInfoItemQueueFactory.createItemQueue(insight.getQueueName());
        return itemQueue.poll(maxResults);
    }

    public RequestValidator<ServiceRequest<GetAgendaRequest>> getRequestValidator()
    {
        return new AgendaServiceValidator();
    }

    public void setInsightPersister(ObjectPersister<Insight> insightPersister)
    {
        this.insightPersister = insightPersister;
    }

    public void setAgendaPersister(ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaPersister = agendaPersister;
    }

    public void setAgendaInfoItemQueueFactory(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
    }
}
