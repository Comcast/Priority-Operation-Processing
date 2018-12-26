package com.theplatform.dfh.cp.endpoint.progress.service;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryRequest;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryResult;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProgressSummaryRequestProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(ProgressSummaryRequestProcessor.class);

    private final HttpCPObjectClient<AgendaProgress> agendaProgressClient;

    public ProgressSummaryRequestProcessor(HttpCPObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    public ProgressSummaryResult getProgressSummary(ProgressSummaryRequest progressSummaryRequest) throws Exception
    {
        if(progressSummaryRequest == null)
        {
            throw new BadRequestException("The request may not be null.");
        }
        if(StringUtils.isBlank(progressSummaryRequest.getLinkId()))
        {
            throw new BadRequestException("The linkId must be specified in the request");
        }

        String queryParams = "?bylinkId=" + progressSummaryRequest.getLinkId();

        DataObjectFeed<AgendaProgress> feed = agendaProgressClient.getObjects(queryParams);

        List<AgendaProgress> progressList = feed.getAll();
        long waiting = progressList.stream().filter(ap -> ap.getProcessingState() == ProcessingState.WAITING).count();
        long executing = progressList.stream().filter(ap -> ap.getProcessingState() == ProcessingState.EXECUTING).count();

        logger.info("Retrieved {} results from query: {} Waiting Count: {} Executing Count: {}",
            feed.getAll().size(), queryParams, waiting, executing);
        ProgressSummaryResult result = new ProgressSummaryResult();
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
}
