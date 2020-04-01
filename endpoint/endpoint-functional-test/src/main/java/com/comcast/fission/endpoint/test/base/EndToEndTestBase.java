package com.comcast.fission.endpoint.test.base;

import com.comcast.fission.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.fission.endpoint.api.progress.ProgressSummaryResponse;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
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
        for(int nCount = 0; nCount < 1000; nCount++)
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
