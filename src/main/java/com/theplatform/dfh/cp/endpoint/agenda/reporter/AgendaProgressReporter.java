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

    public AgendaProgressReporter( ObjectPersister<Agenda> agendaPersister)
    {
        this.agendaPersister = agendaPersister;
    }

    public void logCompletedAgenda(DataObjectResponse<AgendaProgress> response)
    {
        AgendaProgress agendaProgress = response.getFirst();
        if(agendaProgress == null || agendaProgress.getProcessingState() != ProcessingState.COMPLETE)
        {
            return;
        }
        AgendaReporter agendaReporter = new AgendaReporter();
        CaptureLogger captureLogger = new CaptureLogger();
        agendaReporter.setLogger(captureLogger);

        String agendaConclusionStatus = agendaProgress.getProcessingStateMessage();
        String elapsedTime = getElapsedTime(agendaProgress);
        try
        {
            Agenda agenda = agendaPersister.retrieve(agendaProgress.getAgendaId());
            agendaReporter.reportInLine(agenda);
        } catch (Exception | PersistenceException e)
        {
            logger.error("Problem retrieving agenda: " + agendaProgress.getAgendaId(), e);
            return;
        }
        String agendaReportPattern = captureLogger.getMsg();
        logger.info(AgendaReporter.AGENDA_RESPONSE_PREFIX + String.format(agendaReportPattern, agendaConclusionStatus, elapsedTime));
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
}
