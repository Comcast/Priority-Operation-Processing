package com.comcast.pop.endpoint.progress.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comcast.pop.endpoint.validation.ProgressServiceValidator;
import com.comcast.pop.endpoint.base.AbstractServiceRequestProcessor;
import com.comcast.pop.endpoint.base.visibility.CustomerVisibilityFilter;
import com.comcast.pop.endpoint.base.visibility.VisibilityFilter;
import com.comcast.pop.endpoint.client.DataObjectRequestProcessorClient;
import com.comcast.pop.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryResponse;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.query.progress.ByLinkId;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.pop.endpoint.client.ObjectClient;
import com.comcast.pop.persistence.api.ObjectPersister;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ProgressSummaryRequestProcessor extends AbstractServiceRequestProcessor<ProgressSummaryResponse, ServiceRequest<ProgressSummaryRequest>>
{
    private static final Logger logger = LoggerFactory.getLogger(ProgressSummaryRequestProcessor.class);

    private ObjectClient<AgendaProgress> agendaProgressClient;
    private VisibilityFilter<AgendaProgress, ServiceRequest<ProgressSummaryRequest>> visibilityFilter = new CustomerVisibilityFilter<>();

    protected ProgressSummaryRequestProcessor()
    {
        setRequestValidator(new ProgressServiceValidator());
    }

    public ProgressSummaryRequestProcessor(ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister, ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaProgressClient = new DataObjectRequestProcessorClient<>(new AgendaProgressRequestProcessor(
            agendaProgressPersister,
                agendaPersister,
            operationProgressPersister
        ));
        setRequestValidator(new ProgressServiceValidator());
    }

    @Override
    public ProgressSummaryResponse processPOST(ServiceRequest<ProgressSummaryRequest> serviceRequest)
    {
        if(serviceRequest == null)
        {
            return createProgressSummaryResponse(null, ErrorResponseFactory.badRequest("The request may not be null.", null));
        }
        if(StringUtils.isBlank(serviceRequest.getPayload().getLinkId()))
        {
            return createProgressSummaryResponse(null, ErrorResponseFactory.badRequest("The linkId must be specified in the request", serviceRequest.getCID()));
        }

        DataObjectResponse<AgendaProgress> feed = agendaProgressClient.getObjects(Collections.singletonList(new ByLinkId(serviceRequest.getPayload().getLinkId())));

        List<AgendaProgress> progressList = visibilityFilter.filterByVisible(serviceRequest, feed.getAll());
        ProgressSummaryResponse result = createProgressSummaryResponse(progressList, null);

        if(didAnyOperationFail(progressList))
        {
            logger.info("Retrieved {} results from query: {} Failed operation detected.",
                feed.getAll().size(), serviceRequest.getPayload().getLinkId());
            result.setProcessingState(ProcessingState.COMPLETE);
        }
        else
        {
            long waiting = progressList.stream().filter(ap -> ap.getProcessingState() == ProcessingState.WAITING).count();
            long executing = progressList.stream().filter(ap -> ap.getProcessingState() == ProcessingState.EXECUTING).count();
            result.setProcessingPercent(getPercentComplete(progressList));

            logger.info("Retrieved {} results from query: {} Waiting Count: {} Executing Count: {}",
                feed.getAll().size(), serviceRequest.getPayload().getLinkId(), waiting, executing);
            result.setProcessingState(evaluateOverallState(progressList, waiting, executing));
        }
        return result;
    }

    protected double getPercentComplete(List<AgendaProgress> progressList)
    {
        if(progressList == null || progressList.size() == 0)
            return 0d;
        return Math.min(100,
            progressList.stream().filter(ap -> ap.getPercentComplete() != null).mapToDouble(AgendaProgress::getPercentComplete).sum() / progressList.size());
    }

    private ProgressSummaryResponse createProgressSummaryResponse(List<AgendaProgress> progressList, ErrorResponse errorResponse)
    {
        ProgressSummaryResponse progressSummaryResponse = new ProgressSummaryResponse();
        progressSummaryResponse.setErrorResponse(errorResponse);
        progressSummaryResponse.setProgressList(progressList);
        return progressSummaryResponse;
    }

    private boolean didAnyOperationFail(List<AgendaProgress> progressList)
    {
        return progressList.stream()
            .anyMatch(ap -> StringUtils.equalsIgnoreCase(ap.getProcessingStateMessage(), CompleteStateMessage.FAILED.name()));
    }

    private ProcessingState evaluateOverallState(List<AgendaProgress> progressList, long waiting, long executing)
    {
        if(executing > 0)
        {
            return ProcessingState.EXECUTING;
        }
        else if(waiting > 0)
        {
            return waiting == progressList.size() ? ProcessingState.WAITING : ProcessingState.EXECUTING;
        }
        return ProcessingState.COMPLETE;
    }

    public void setAgendaProgressClient(ObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    public ProgressSummaryRequestProcessor setVisibilityFilter(
        VisibilityFilter<AgendaProgress, ServiceRequest<ProgressSummaryRequest>> visibilityFilter)
    {
        this.visibilityFilter = visibilityFilter;
        return this;
    }
}
