package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.Report;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.CustomerVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilter;
import com.theplatform.dfh.cp.endpoint.validation.AgendaServiceValidator;
import com.theplatform.dfh.endpoint.api.*;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Agenda service request processor
 */
public class GetAgendaServiceRequestProcessor extends RequestProcessor<GetAgendaResponse, ServiceRequest<GetAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(GetAgendaServiceRequestProcessor.class);
    private static final String AGENDA_REQUEST_TEMPLATE = "Agenda Request metadata - insightid=%s agendarequestcount=%d";
    private static final String AUTHORIZATION_EXCEPTION = "You do not have permission to perform this action for customerId %1$s";

    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<ResourcePool> resourcePoolPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory;
    private VisibilityFilter<Insight, ServiceRequest<GetAgendaRequest>> insightVisibilityFilter = new CustomerVisibilityFilter<>();
    private VisibilityFilter<ResourcePool, ServiceRequest<GetAgendaRequest>> resourcePoolVisibilityFilter = new CustomerVisibilityFilter<>();

    public GetAgendaServiceRequestProcessor(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Agenda> agendaPersister, ObjectPersister<ResourcePool> resourcePoolPersister)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
        this.insightPersister = insightPersister;
        this.resourcePoolPersister = resourcePoolPersister;
        this.agendaPersister = agendaPersister;
    }

    @Override
    protected GetAgendaResponse handlePOST(ServiceRequest<GetAgendaRequest> serviceRequest)
    {
        GetAgendaRequest getAgendaRequest = serviceRequest.getPayload();
        if (getAgendaRequest.getInsightId() == null)
        {
            final String message = "No insight id provided.  Cannot process getAgenda request.";
            logger.warn(message);
            return new GetAgendaResponse(ErrorResponseFactory.badRequest(message, serviceRequest.getCID()));
        }

        logger.info(String.format(AGENDA_REQUEST_TEMPLATE, getAgendaRequest.getInsightId(), getAgendaRequest.getCount()));

        Insight insight;
        try
        {
            insight = insightPersister.retrieve(getAgendaRequest.getInsightId());
        }
        catch(PersistenceException e)
        {
            ErrorResponse errorResponse = ErrorResponseFactory.buildErrorResponse(e, 400, serviceRequest.getCID());
            logger.warn(errorResponse.getServerStackTrace());
            return new GetAgendaResponse(errorResponse);
        }

        if(insight == null)
        {
            final String message = String.format("No insight found with id %s. Cannot process getAgenda request.",
            getAgendaRequest.getInsightId());
            logger.warn(message);
            return new GetAgendaResponse(ErrorResponseFactory.objectNotFound(message, serviceRequest.getCID()));
        }
        GetAgendaResponse response = isVisible(insight, serviceRequest);
        if(response != null)
            return response;

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
                return new GetAgendaResponse(agendaList);
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

    private GetAgendaResponse isVisible(Insight insight, ServiceRequest<GetAgendaRequest> serviceRequest)
    {
        //Look up the resource pool for visibility
        ResourcePool resourcePool;
        try
        {
            resourcePool = resourcePoolPersister.retrieve(insight.getResourcePoolId());
            if(resourcePool == null)
                return new GetAgendaResponse(ErrorResponseFactory.objectNotFound(String.format("No resource pool found with insight id %s. Cannot process getAgenda request.",
                    insight.getId()), serviceRequest.getCID()));
        }
        catch(PersistenceException e)
        {
            return new GetAgendaResponse(ErrorResponseFactory.buildErrorResponse(e, 400, serviceRequest.getCID()));
        }
        if(!insightVisibilityFilter.isVisible(serviceRequest, insight) && !resourcePoolVisibilityFilter.isVisible(serviceRequest, resourcePool))
        {
            return new GetAgendaResponse((ErrorResponseFactory.unauthorized(String.format(AUTHORIZATION_EXCEPTION, insight.getCustomerId()), serviceRequest.getCID())));
        }
        return null;
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
