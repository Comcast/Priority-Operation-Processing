package com.comcast.fission.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.progress.reporter.BaseReporterThread;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Agenda progress threaded reporter
 */
public class AgendaProgressThread extends BaseReporterThread<AgendaProgressThreadConfig> implements AgendaProgressConsumer
{
    // reporter thread fields (not to be used in other threads without adjusting the use of synchronized in this class)
    private AgendaProgressThreadConfig agendaProgressThreadConfig;
    private List<OperationProgress> operationProgressToReport = new LinkedList<>();
    private boolean completeProgressSent = false;

    // cross thread fields
    private Set<OperationProgressProvider> operationProgressProviders = new HashSet<>();
    private AgendaProgress agendaProgress;

    // operation percent completion tracking
    private int totalProgressOperationCount = 0;
    private int completedProgressOperationCount = 0;
    private double overallProgressPercentComplete = 0d;

    public AgendaProgressThread(AgendaProgressThreadConfig agendaProgressThreadConfig)
    {
        super(agendaProgressThreadConfig);
        this.agendaProgressThreadConfig = agendaProgressThreadConfig;
    }

    @Override
    protected boolean isThereProgressToReport()
    {
        return operationProgressToReport.size() > 0 || !completeProgressSent;
    }

    protected void resetOperationProgressToReport()
    {
        operationProgressToReport.clear();
    }

    private synchronized AgendaProgress getAgendaProgress()
    {
        return agendaProgress;
    }

    @Override
    public synchronized void setAgendaProgress(AgendaProgress agendaProgress)
    {
        this.agendaProgress = agendaProgress;
    }

    @Override
    public synchronized void registerOperationProgressProvider(OperationProgressProvider operationProgressProvder)
    {
        this.operationProgressProviders.add(operationProgressProvder);
    }

    protected synchronized Set<OperationProgressProvider> getOperationProgressProviders()
    {
        return operationProgressProviders;
    }

    protected synchronized void setOperationProgressProviders(Set<OperationProgressProvider> operationProgressProviders)
    {
        this.operationProgressProviders = operationProgressProviders;
    }

    protected List<OperationProgress> getOperationProgressToReport()
    {
        return operationProgressToReport;
    }

    protected void setOperationProgressToReport(List<OperationProgress> operationProgressToReport)
    {
        this.operationProgressToReport = operationProgressToReport;
    }

    @Override
    protected synchronized void updateProgressItemsToReport()
    {
        if(operationProgressProviders == null || operationProgressProviders.size() == 0) return;

        List<OperationProgressProvider> operationProgressRetrieversToRemove = new LinkedList<>();

        double incompleteOperationPercentTotal = 0;

        for(OperationProgressProvider opr : operationProgressProviders)
        {
            OperationProgress operationProgress = opr.retrieveOperationProgress();
            if(operationProgress != null
                && operationProgress.getProcessingState() != null)
            {
                switch (operationProgress.getProcessingState())
                {
                    case EXECUTING:
                        // remove any existing progress for the same op
                        operationProgressToReport.removeIf(op -> StringUtils.equals(operationProgress.getOperation(), op.getOperation()));
                        if(operationProgress.getPercentComplete() != null)
                        {
                            // protect against handlers saying percents over 100
                            incompleteOperationPercentTotal += Math.min(100d, operationProgress.getPercentComplete());
                        }
                        operationProgressToReport.add(operationProgress);
                        break;
                    case COMPLETE:
                        processCompleteOperationProgress(operationProgress);
                        completedProgressOperationCount++;
                        operationProgressToReport.removeIf(op -> StringUtils.equals(operationProgress.getOperation(), op.getOperation()));
                        operationProgressToReport.add(operationProgress);
                        // completed operations have no further progress to report
                        operationProgressRetrieversToRemove.add(opr);
                        break;
                }
            }
        }

        overallProgressPercentComplete = calculatePercentComplete(completedProgressOperationCount, totalProgressOperationCount, incompleteOperationPercentTotal);

        operationProgressRetrieversToRemove.forEach(opr -> operationProgressProviders.remove(opr));
    }

    protected void processCompleteOperationProgress(OperationProgress operationProgress)
    {
        // complete + succeeded is always pushed to 100%
        if(StringUtils.equalsIgnoreCase(CompleteStateMessage.SUCCEEDED.toString(), operationProgress.getProcessingStateMessage()))
        {
            operationProgress.setPercentComplete(100d);
        }
    }

    // if this becomes any more complex break it into its own class
    protected static double calculatePercentComplete(int completedOperationCount, int totalOperationCount, double incompleteOperationPercentTotal)
    {
        return (totalOperationCount > 0)
            ? ((completedOperationCount * 100d) + incompleteOperationPercentTotal) / totalOperationCount
            : 0d;
    }

    @Override
    protected void reportProgress()
    {
        AgendaProgress agendaProgress = getAgendaProgress();

        if(agendaProgress == null) return;

        // attach the latest progress value based on the operations
        agendaProgress.setPercentComplete(overallProgressPercentComplete);

        if(operationProgressToReport.size() > 0)
        {
            // TODO: do not set the agendaProgressId on the ops progress, make the progress endpoint deal with that
            agendaProgress.setOperationProgress(operationProgressToReport.toArray(new OperationProgress[0]));
        }

        if(agendaProgressThreadConfig.getRequireProgressId() && agendaProgress.getId() == null)
        {
            logger.warn("The AgendaProgress is unset. No update is being sent.");
        }
        else
        {
            logger.trace("Reporting progress here. LinkId:[" + agendaProgress.getLinkId() + "], agendaId: [" +
                agendaProgress.getAgendaId() + "], Id: [" + agendaProgress.getId() + "], Cid: [" + agendaProgress.getCid() + "]");
            agendaProgressThreadConfig.getReporter().reportProgress(agendaProgress);
        }

        logger.debug(
            "Reported progress {} {}% for: {} with progress updates for ops: {}",
            agendaProgress.getProcessingState(),
            agendaProgress.getPercentComplete(),
            agendaProgress.getId(),
            operationProgressToReport == null
            ? null
            : operationProgressToReport.stream().map(OperationProgress::getOperation).collect(Collectors.joining(","))
        );


        if(agendaProgress.getProcessingState() == ProcessingState.COMPLETE)
        {
            completeProgressSent = true;
        }

        logger.debug("Clearing progress to report");
        // on success we clear the op progress to report
        resetOperationProgressToReport();
    }

    @Override
    public synchronized void setTotalProgressOperationCount(int totalProgressOperationCount)
    {
        this.totalProgressOperationCount = totalProgressOperationCount;
    }

    @Override
    public synchronized void adjustTotalProgressOperationCount(int operationTotalCountAdjustment)
    {
        setTotalProgressOperationCount(totalProgressOperationCount + operationTotalCountAdjustment);
    }

    @Override
    public synchronized void incrementCompletedOperationCount(int incrementAmount)
    {
        completedProgressOperationCount += incrementAmount;
    }

    @Override
    protected String getThreadName()
    {
        return "AgendaProgressThread";
    }
}
