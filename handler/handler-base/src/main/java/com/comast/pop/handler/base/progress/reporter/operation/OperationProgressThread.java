package com.comast.pop.handler.base.progress.reporter.operation;

import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comast.pop.handler.base.progress.reporter.BaseReporterThread;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;

/**
 * Operation progress threaded reporter
 *
 * Any progress can be added and may or may not be reported
 * The progress with a complete status will be reported no matter what (with the exception of multiple failed attempts)
 */
public class OperationProgressThread extends BaseReporterThread<OperationProgressThreadConfig> implements OperationProgressConsumer
{
    private OperationProgress operationProgress;
    private boolean hasCompleteOperationProgress = false;
    private Object payload = null;

    public OperationProgressThread(OperationProgressThreadConfig operationProgressThreadConfig)
    {
        super(operationProgressThreadConfig);
    }

    protected synchronized OperationProgress getOperationProgress()
    {
        return operationProgress;
    }

    /**
     * Sets the operation progress unless the a progress with the completed status has been sent.
     * @param operationProgress The progress to set (this may or may not be reported, unless it is the complete progress)
     * @param payload The payload to send (only applies if the progress state is complete)
     */
    public synchronized void setOperationProgress(OperationProgress operationProgress, Object payload)
    {
        // reject all progress after the last
        if(hasCompleteOperationProgress) return;
        if(operationProgress.getProcessingState() == ProcessingState.COMPLETE)
        {
            hasCompleteOperationProgress = true;
            this.payload = payload;
        }
        this.operationProgress = operationProgress;
    }

    /**
     * Sets the operation progress unless the a progress with the completed status has been sent.
     * @param operationProgress The progress to set (this may or may not be reported, unless it is the complete progress)
     */
    public synchronized void setOperationProgress(OperationProgress operationProgress)
    {
        setOperationProgress(operationProgress, null);
    }

    /**
     * Resets the progress unless the outstanding operation is status:complete and last reported is not
     * @param lastOperationProgressReported the last successful progress reported
     */
    protected synchronized void resetOperationProgress(OperationProgress lastOperationProgressReported)
    {
        // if the pending progress is complete and it has not been reported skip the reset
        if(lastOperationProgressReported.getProcessingState() != ProcessingState.COMPLETE &&
            operationProgress.getProcessingState() == ProcessingState.COMPLETE)
        {
            return;
        }
        operationProgress = null;
    }

    @Override
    protected synchronized boolean isThereProgressToReport()
    {
        // the reporter is only done if the complete progress has been added and is null (indicating sent)
        return !(hasCompleteOperationProgress && operationProgress == null);
    }

    @Override
    protected void reportProgress()
    {
        OperationProgress operationProgressToReport = getOperationProgress();
        if(null == operationProgressToReport) return;

        if(payload == null)
        {
            getProgressReporterConfig().getReporter().reportProgress(operationProgressToReport);
        }
        else
        {
            getProgressReporterConfig().getReporter().reportProgress(operationProgressToReport, payload);
        }
        // if the above succeeds reset the progress
        resetOperationProgress(operationProgressToReport);
    }

    @Override
    protected void onLostProgress()
    {
        OperationProgress operationProgressToReport = getOperationProgress();
        try
        {
            logger.error("Unable to report progress: {} (completeProgressSeen: {})",
                operationProgressToReport == null
                ? "None"
                : new JsonHelper().getJSONString(operationProgressToReport),
                hasCompleteOperationProgress);
        }
        catch(Throwable t)
        {
            logger.error("Failed to log lost progress.");
        }
    }

    @Override
    protected String getThreadName()
    {
        return "OperationProgressThread";
    }
}
