package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaReporter
{
    protected Logger logger = LoggerFactory.getLogger(AgendaReporter.class);
    private static final AgendaReports[] DEFAULT_REPORT = {
            AgendaReports.CID,
            AgendaReports.AGENDA_ID,
            AgendaReports.CUSTOMER_ID,
            AgendaReports.AGENDA_TYPE};

    private String prefix;
    private AgendaReports[] agendaReports = DEFAULT_REPORT;
    private String agendaState;

    public AgendaReporter(String prefix, AgendaReports... agendaReports)
    {
        this.prefix = prefix;
        if(agendaReports != null && agendaReports.length > 0)
        {
            this.agendaReports = agendaReports;
        }
    }

    public void report(Agenda agenda)
    {
        AgendaReportData agendaReportData = new AgendaReportData(agenda, agendaState);
        for(Report<AgendaReportData, String> report: agendaReports)
        {
            logger.info(prefix + report.report(agendaReportData));
        }
    }

    public void reportInLine(Agenda agenda)
    {
        AgendaReportData agendaReportData = new AgendaReportData(agenda, agendaState);
        StringBuilder b = new StringBuilder();
        b.append(prefix).append("[");
        for(int i = 0; i < agendaReports.length;i++)
        {
            Report<AgendaReportData,String> report = agendaReports[i];
            b.append(report.report(agendaReportData));
            if(i < agendaReports.length - 1)
            {
                b.append("; ");
            }
        }

        b.append("]");
        logger.info(b.toString());
    }

    public void setProcessingState(String agendaState)
    {
        this.agendaState = agendaState;
    }
}
class AgendaReportData
{

    private Agenda agenda;
    private String agendaState;

    public AgendaReportData(Agenda agenda, String agendaState)
    {

        this.agenda = agenda;
        this.agendaState = agendaState;
    }

    public Agenda getAgenda()
    {
        return agenda;
    }

    public String getAgendaState()
    {
        return agendaState;
    }
}
