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

}
