package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaReporter
{
    private static Logger logger = LoggerFactory.getLogger(AgendaReporter.class);
    private static final AgendaReports[] DEFAULT_REPORT = {AgendaReports.AGENDA_ID, AgendaReports.CID};

    private String prefix;
    private AgendaReports[] agendaReports = DEFAULT_REPORT;

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
        for(Report<Agenda, String> report: agendaReports)
        {
            logger.info(prefix + report.report(agenda));
        }
    }

    public void reportInLine(Agenda agenda)
    {
        StringBuilder b = new StringBuilder();
        b.append(prefix).append("[");
        for(int i = 0; i < agendaReports.length;i++)
        {
            Report<Agenda,String> report = agendaReports[i];
            b.append(report.report(agenda));
            if(i < agendaReports.length - 1)
            {
                b.append("; ");
            }
        }
        b.append("]");
        logger.info(b.toString());
    }
}
