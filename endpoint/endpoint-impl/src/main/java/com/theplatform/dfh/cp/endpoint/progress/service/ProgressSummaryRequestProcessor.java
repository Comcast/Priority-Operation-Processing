package com.theplatform.dfh.cp.endpoint.progress.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.base.AbstractServiceRequestProcessor;
import com.theplatform.dfh.cp.endpoint.base.visibility.CustomerVisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilter;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.comcast.fission.endpoint.api.progress.ProgressSummaryResponse;
import com.theplatform.dfh.cp.endpoint.validation.ProgressServiceValidator;
import com.comcast.fission.endpoint.api.ErrorResponse;
import com.comcast.fission.endpoint.api.ErrorResponseFactory;
import com.comcast.fission.endpoint.api.ServiceRequest;
import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.api.data.query.progress.ByLinkId;
import com.comcast.fission.endpoint.api.progress.ProgressSummaryRequest;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
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
