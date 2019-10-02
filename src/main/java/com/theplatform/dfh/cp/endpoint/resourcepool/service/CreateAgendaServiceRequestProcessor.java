package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.EndpointDataObjectFeed;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.CustomerVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilter;
import com.theplatform.dfh.endpoint.api.*;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Agenda service request processor for creating agendas
 */
public class CreateAgendaServiceRequestProcessor extends RequestProcessor<DataObjectFeedServiceResponse<Agenda>, ServiceRequest<EndpointDataObjectFeed<Agenda>>>
{
    private static final Logger logger = LoggerFactory.getLogger(CreateAgendaServiceRequestProcessor.class);
    private static final String AGENDA_REQUEST_TEMPLATE = "Agenda Request metadata - insightid=%s agendarequestcount=%d";
    private static final String AUTHORIZATION_EXCEPTION = "You do not have permission to perform this action for customerId %1$s";

    private ObjectPersister<Insight> insightPersister;
    private ObjectPersister<ResourcePool> resourcePoolPersister;
    private ObjectPersister<Agenda> agendaPersister;
    private ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory;
    private VisibilityFilter<Insight, ServiceRequest<GetAgendaRequest>> insightVisibilityFilter = new CustomerVisibilityFilter<>();
    private VisibilityFilter<ResourcePool, ServiceRequest<GetAgendaRequest>> resourcePoolVisibilityFilter = new CustomerVisibilityFilter<>();

    public CreateAgendaServiceRequestProcessor(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Agenda> agendaPersister, ObjectPersister<ResourcePool> resourcePoolPersister)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
        this.insightPersister = insightPersister;
        this.resourcePoolPersister = resourcePoolPersister;
        this.agendaPersister = agendaPersister;
    }

    @Override
    protected DataObjectFeedServiceResponse<Agenda> handlePOST(ServiceRequest<EndpointDataObjectFeed<Agenda>> serviceRequest)
    {
        EndpointDataObjectFeed<Agenda> getAgendaRequest = serviceRequest.getPayload();

        //loop through each agenda and look up
//@todo
        return null;
    }

    private DataObjectFeedServiceResponse<Agenda> isVisible(Insight insight, ServiceRequest<GetAgendaRequest> serviceRequest)
    {
        //Look up the resource pool for visibility
        ResourcePool resourcePool;
        try
        {
            resourcePool = resourcePoolPersister.retrieve(insight.getResourcePoolId());
            if(resourcePool == null)
                return new DataObjectFeedServiceResponse<>(ErrorResponseFactory.objectNotFound(String.format("No resource pool found with insight id %s. Cannot process getAgenda request.",
                    insight.getId()), serviceRequest.getCID()));
        }
        catch(PersistenceException e)
        {
            return new DataObjectFeedServiceResponse<>(ErrorResponseFactory.buildErrorResponse(e, 400, serviceRequest.getCID()));
        }
        if(!insightVisibilityFilter.isVisible(serviceRequest, insight) && !resourcePoolVisibilityFilter.isVisible(serviceRequest, resourcePool))
        {
            return new DataObjectFeedServiceResponse<>((ErrorResponseFactory.unauthorized(String.format(AUTHORIZATION_EXCEPTION, insight.getCustomerId()), serviceRequest.getCID())));
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

    public RequestValidator<ServiceRequest<EndpointDataObjectFeed<Agenda>>> getRequestValidator()
    {
        return null;
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

