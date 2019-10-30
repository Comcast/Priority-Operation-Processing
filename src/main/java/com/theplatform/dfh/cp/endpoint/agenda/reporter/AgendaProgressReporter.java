package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class AgendaProgressReporter
{
    protected static Logger logger = LoggerFactory.getLogger(AgendaProgressReporter.class);

    private ObjectPersister<Agenda> agendaPersister;
    private ObjectPersister<AgendaProgress> agendaProgressPersister;

    public AgendaProgressReporter( ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaPersister = agendaPersister;
    }

    public void logCompletedAgenda(DataObjectResponse<AgendaProgress> response)
    {
        logCompletedAgenda(response.getFirst());
    }

    public void logCompletedAgenda(AgendaProgress partialAgendaProgress)
    {
        if(partialAgendaProgress == null || partialAgendaProgress.getProcessingState() != ProcessingState.COMPLETE)
        {
            return;
        }
        AgendaProgress fullAgendaProgress;
        try
        {
             fullAgendaProgress = agendaProgressPersister.retrieve(partialAgendaProgress.getId());
        } catch (Exception | PersistenceException e)
        {
            logger.error("Problem retrieving full agendaProgress: " + partialAgendaProgress.getId(), e);
            return;
        }

        AgendaReporter agendaReporter = new AgendaReporter();

        String agendaConclusionStatus = partialAgendaProgress.getProcessingStateMessage();
        String elapsedTime = getElapsedTime(fullAgendaProgress);
        String agendaReportPattern;
        try
        {
            Agenda agenda = agendaPersister.retrieve(fullAgendaProgress.getAgendaId());
            agendaReportPattern = agendaReporter.reportInLine(agenda);
        } catch (Exception | PersistenceException e)
        {
            logger.error("Problem retrieving agenda: " + fullAgendaProgress.getAgendaId(), e);
            return;
        }

        String[] inputs = new String[]{agendaConclusionStatus, elapsedTime};
        if(inputCountMatchesPattern(agendaReportPattern, inputs.length))
        {
            logger.info(AgendaReporter.AGENDA_RESPONSE_PREFIX + String.format(agendaReportPattern, inputs));
        }else
        {
            logger.warn("Agenda progress input count does not match report pattern: " + agendaReportPattern);
        }
    }

    private boolean inputCountMatchesPattern(String agendaReportPattern, int length)
    {
        if(agendaReportPattern == null)
        {
            return false;
        }
        return agendaReportPattern.replaceAll("%s", "a%sa").split("%s").length == length + 1;
    }

    protected String getElapsedTime(AgendaProgress agendaProgress)
    {
        String elapsedTime = "Elapsed exec time not available for agenda: " + agendaProgress.getAgendaId();
        Date startDate = agendaProgress.getStartedTime();
        Date endDate = agendaProgress.getCompletedTime();
        if(startDate != null && endDate != null)
        {
            long startTime = startDate.getTime();
            long endTime = endDate.getTime();
            Long duration = endTime - startTime;
            elapsedTime = duration.toString();
        }
        return elapsedTime;
    }

    public void setAgendaProgressPersister(ObjectPersister<AgendaProgress> agendaProgressPersister)
    {
        this.agendaProgressPersister = agendaProgressPersister;
    }
}
