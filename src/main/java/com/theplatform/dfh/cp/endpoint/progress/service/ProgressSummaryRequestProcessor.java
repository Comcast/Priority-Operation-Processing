package com.theplatform.dfh.cp.endpoint.progress.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.client.DataObjectRequestProcessorClient;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryResponse;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.progress.ByLinkId;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryRequest;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class ProgressSummaryRequestProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(ProgressSummaryRequestProcessor.class);

    private ObjectClient<AgendaProgress> agendaProgressClient;

    protected ProgressSummaryRequestProcessor()
    {

    }

    public ProgressSummaryRequestProcessor(ObjectPersister<AgendaProgress> agendaProgressPersister,
        ObjectPersister<OperationProgress> operationProgressPersister)
    {
        this.agendaProgressClient = new DataObjectRequestProcessorClient<>(new AgendaProgressRequestProcessor(
            agendaProgressPersister,
            operationProgressPersister
        ));
    }

    public ProgressSummaryResponse getProgressSummary(ServiceRequest<ProgressSummaryRequest> progressSummaryRequest) throws Exception
    {
        if(progressSummaryRequest == null)
        {
            return new ProgressSummaryResponse(ErrorResponseFactory.badRequest("The request may not be null.", null));
        }
        if(StringUtils.isBlank(progressSummaryRequest.getPayload().getLinkId()))
        {
            return new ProgressSummaryResponse(ErrorResponseFactory.badRequest("The linkId must be specified in the request", progressSummaryRequest.getCID()));
        }

        DataObjectResponse<AgendaProgress> feed = agendaProgressClient.getObjects(Collections.singletonList(new ByLinkId(progressSummaryRequest.getPayload().getLinkId())));

        List<AgendaProgress> progressList = feed.getAll();
        long waiting = progressList.stream().filter(ap -> ap.getProcessingState() == ProcessingState.WAITING).count();
        long executing = progressList.stream().filter(ap -> ap.getProcessingState() == ProcessingState.EXECUTING).count();

        logger.info("Retrieved {} results from query: {} Waiting Count: {} Executing Count: {}",
            feed.getAll().size(), progressSummaryRequest.getPayload().getLinkId(), waiting, executing);
        ProgressSummaryResponse result = new ProgressSummaryResponse();
        result.setProgressList(feed.getAll());
        result.setProcessingState(evaluateOverallState(progressList, waiting, executing));
        return result;
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
}
