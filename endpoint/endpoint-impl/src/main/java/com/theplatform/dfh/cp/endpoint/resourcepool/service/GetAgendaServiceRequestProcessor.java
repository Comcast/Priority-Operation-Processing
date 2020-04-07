package com.theplatform.dfh.cp.endpoint.resourcepool.service;

import com.comcast.pop.endpoint.api.BadRequestException;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.agenda.reporter.Report;
import com.comcast.pop.endpoint.base.AbstractServiceRequestProcessor;
import com.comcast.pop.endpoint.base.validation.RequestValidator;
import com.comcast.pop.endpoint.base.visibility.NoOpVisibilityFilter;
import com.comcast.pop.endpoint.base.visibility.VisibilityFilterMap;
import com.comcast.pop.endpoint.base.visibility.VisibilityMethod;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.resourcepool.InsightRequestProcessor;
import com.theplatform.dfh.cp.endpoint.validation.AgendaServiceGetAgendaValidator;
import com.theplatform.dfh.cp.scheduling.api.AgendaInfo;
import com.comcast.pop.endpoint.api.auth.AuthorizationResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaRequest;
import com.comcast.pop.endpoint.api.resourcepool.GetAgendaResponse;
import com.theplatform.dfh.modules.queue.api.ItemQueue;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.modules.queue.api.QueueResult;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
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
    private AgendaProgressRequestProcessor agendaProgressRequestProcessor;
    private ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory;

    public GetAgendaServiceRequestProcessor(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory, ObjectPersister<Insight> insightPersister,
        ObjectPersister<Agenda> agendaPersister, ObjectPersister<AgendaProgress> agendaProgressPersister, ObjectPersister<OperationProgress> operationProgressPersister)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
        this.agendaPersister = agendaPersister;

        this.agendaProgressRequestProcessor = new AgendaProgressRequestProcessor(agendaProgressPersister, agendaPersister, operationProgressPersister);
        //override visibility for GET as the id on the Agenda is from the data store so the agendaProgressId should be legit
        agendaProgressRequestProcessor.setVisibilityFilter(VisibilityMethod.GET, new NoOpVisibilityFilter<>());

        this.insightRequestProcessor = new InsightRequestProcessor(insightPersister);
        //override the default visibility filter since it's too visible for the calling user
        this.insightRequestProcessor.setVisibilityFilterMap(new VisibilityFilterMap<>());
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
        addErrorForObjectNotFound(insightResponse, Insight.class, getAgendaRequest.getInsightId(), serviceRequest.getCID());
        if(insightResponse.isError()) return new GetAgendaResponse(insightResponse.getErrorResponse());
        Insight insight = insightResponse.getFirst();

        try
        {
            QueueResult<AgendaInfo> agendaInfoQueueResult = pollInsightQueue(insight, getAgendaRequest.getCount());
            if(agendaInfoQueueResult.isSuccessful())
            {
                List<Agenda> agendaList = new LinkedList<>();
                List<AgendaProgress> agendaProgresses = new LinkedList<>();
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
                            retrieveExistingAgendaProgress(agenda, agendaProgresses);
                            agendaList.add(agenda);
                        }
                    }
                }
                return new GetAgendaResponse(
                    agendaList,
                    agendaProgresses.size() == 0
                        ? null
                        : agendaProgresses);
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

    private Agenda retrieveAgenda(String agendaId) throws PersistenceException
    {
        return agendaPersister.retrieve(agendaId);
    }

    /**
     * Retrieves the agendaProgress associated with the Agenda and appends it to the passed in list.
     * Only appends progress that has operations wtih existing progress
     * @param agenda The Agenda to lookup the progress for
     * @param agendaProgresses The list to append with progress if it exists
     */
    protected void retrieveExistingAgendaProgress(Agenda agenda, List<AgendaProgress> agendaProgresses) throws PersistenceException
    {
        logger.info("Attempting to lookup agendaProgressId={}", agenda.getProgressId());
        DataObjectResponse<AgendaProgress> response = agendaProgressRequestProcessor.handleGET(
            new DefaultDataObjectRequest<>(null, agenda.getProgressId(), null));

        boolean foundProgress = false;
        if(response.isError())
        {
            // this is not considered a critical failure
            logger.warn("Failed to look up agendaProgressId={} {}", agenda.getProgressId(), response.getErrorResponse().getDescription());
        }
        else
        {
            AgendaProgress agendaProgress = response.getFirst();
            if (agendaProgress != null && agendaProgress.getOperationProgress() != null)
            {
                // only add the AgendaProgress if one of the OperationProgress is non-waiting
                if (Arrays.stream(agendaProgress.getOperationProgress())
                    .anyMatch(op -> ProcessingState.WAITING != op.getProcessingState()))
                {
                    foundProgress = true;
                    agendaProgresses.add(agendaProgress);
                }
            }
        }
        logger.info("{}Existing progress found for agendaId={}",
            foundProgress ? "" : "No ",
            agenda.getId());
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
        return new AgendaServiceGetAgendaValidator();
    }

    public void setAgendaPersister(ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaPersister = agendaPersister;
    }

    protected void setAgendaProgressRequestProcessor(AgendaProgressRequestProcessor agendaProgressRequestProcessor)
    {
        this.agendaProgressRequestProcessor = agendaProgressRequestProcessor;
    }

    public void setInsightRequestProcessor(InsightRequestProcessor insightRequestProcessor)
    {
        this.insightRequestProcessor = insightRequestProcessor;
    }

    public void setAgendaInfoItemQueueFactory(ItemQueueFactory<AgendaInfo> agendaInfoItemQueueFactory)
    {
        this.agendaInfoItemQueueFactory = agendaInfoItemQueueFactory;
    }
}
