package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.Report;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.validation.RequestValidator;
import com.theplatform.dfh.cp.endpoint.base.visibility.CustomerVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilterMap;
import com.theplatform.dfh.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.theplatform.dfh.cp.endpoint.validation.AgendaServiceValidator;
import com.theplatform.dfh.endpoint.api.*;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.theplatform.dfh.endpoint.api.auth.AuthorizationResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
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
 * A resource pool puller needs to pull for it's visible agenda's. The request includes the insightID for the
 * mapped Agendas to that Insight.
 * In order to securely access the Agendas we verify the calling user's authorized accounts have visibility access
 * to the Insight. This is for owned Insights. global=true does not grant access for the calling user, nor does allowedCustomerIDs.
 * That is for customer specific visibility.
 */
public class GetAgendaServiceRequestProcessor extends AbstractServiceRequestProcessor<GetAgendaResponse, ServiceRequest<GetAgendaRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(GetAgendaServiceRequestProcessor.class);
    private static final String AGENDA_REQUEST_TEMPLATE = "Agenda Request metadata - insightid=%s agendarequestcount=%d";

    private InsightRequestProcessor insightRequestProcessor;
    private ObjectPersister<Agenda> agendaPersister;
    private ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory;

    public GetAgendaServiceRequestProcessor(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
        this.insightRequestProcessor = new InsightRequestProcessor(insightPersister);
        //override the default visibility filter since it's too visible for the calling user
        this.insightRequestProcessor.setVisibilityFilterMap(new VisibilityFilterMap<>());
        this.agendaPersister = agendaPersister;
    }

    @Override
    public GetAgendaResponse processPOST(ServiceRequest<GetAgendaRequest> serviceRequest)
    {
        GetAgendaRequest getAgendaRequest = serviceRequest.getPayload();
        if (getAgendaRequest.getInsightId() == null)
        {
            final String message = "No insight id provided.  Cannot process getAgenda request.";
            logger.warn(message);
            return new GetAgendaResponse(ErrorResponseFactory.badRequest(message, serviceRequest.getCID()));
        }

        logger.info(String.format(AGENDA_REQUEST_TEMPLATE, getAgendaRequest.getInsightId(), getAgendaRequest.getCount()));

        //We need to verify the calling user is Authorized for the Insight
        DataObjectResponse<Insight> insightResponse = insightRequestProcessor.handleGET(generateInsightReq(serviceRequest.getAuthorizationResponse(),
                getAgendaRequest.getInsightId()));
        ErrorResponse errorResponse = checkForRetrieveError(insightResponse, Insight.class, getAgendaRequest.getInsightId(), serviceRequest.getCID());
        if(errorResponse != null) return new GetAgendaResponse(errorResponse);
        Insight insight = insightResponse.getFirst();

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

    private DefaultDataObjectRequest<Insight> generateInsightReq(AuthorizationResponse authorizationResponse, String insightId)
    {
        DefaultDataObjectRequest<Insight> req = new DefaultDataObjectRequest<>();
        req.setAuthorizationResponse(authorizationResponse);
        req.setId(insightId);
        return req;
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

    public void setAgendaPersister(ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaPersister = agendaPersister;
    }

    public void setAgendaInfoItemQueueFactory(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
    }
}
