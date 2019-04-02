package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaReporter
{
    private static Logger logger = LoggerFactory.getLogger(AgendaReporter.class);

    private String prefix;
    private AgendaReports[] agendaReports;

    public AgendaReporter(String prefix, AgendaReports... agendaReports)
    {
        this.prefix = prefix;
        this.agendaReports = agendaReports;
    }

    public void report(Agenda agenda)
    {
        for(AgendaReport report: agendaReports)
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
            AgendaReport report = agendaReports[i];
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
