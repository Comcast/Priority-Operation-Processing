package com.comcast.pop.endpoint.test.base;

import com.comcast.pop.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryResponse;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.ProcessingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 */
public class EndToEndTestBase extends EndpointTestBase
{
    private static final Logger logger = LoggerFactory.getLogger(EndToEndTestBase.class);

    protected void waitOnStatus(String linkId, final ProcessingState processingState) throws Exception
    {
        for(int nCount = 0; nCount < 100; nCount++)
        {
            Thread.sleep(1500);
            ProgressSummaryResponse progressSummaryResult = progressServiceClient.getProgressSummary(new ProgressSummaryRequest(linkId));
            logger.info("AgendaProgress: linkid: {} - {} ", linkId, progressSummaryResult.getProcessingState());
            if(processingState.equals(progressSummaryResult.getProcessingState()))
            {
                logger.info("Complete!");
                break;
            }
        }
    }

    /**
     * This is just a test for watching a specific progress id
     * @throws Exception
     */
    @Test(enabled = false)
    public void watchProgressId() throws Exception
    {
        final String progressId = "";
        for(;;)
        {
            Thread.sleep(1500);
            AgendaProgress agendaProgress = agendaProgressClient.getObject(progressId).getFirst();
            logger.info("AgendaProgress: {} - {} ", progressId, agendaProgress.getProcessingState());
            if(ProcessingState.COMPLETE.equals(agendaProgress.getProcessingState()))
            {
                logger.info("Complete!");
                break;
            }
        }
    }
}
