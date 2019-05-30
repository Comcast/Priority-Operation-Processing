package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaReporter
{
    protected Logger logger = LoggerFactory.getLogger(AgendaReporter.class);

    // todo needed?
    private static final AgendaReports[] DEFAULT_REPORT = {
            AgendaReports.CID,
            AgendaReports.AGENDA_ID,
            AgendaReports.CUSTOMER_ID,
            AgendaReports.AGENDA_TYPE};

    public static final Report[] AGENDA_REPORTS = new Report[]{
            AgendaReports.CID,
            AgendaReports.AGENDA_ID,
            AgendaReports.LINK_ID,
            AgendaReports.CUSTOMER_ID,
            AgendaReports.AGENDA_STATUS_PATTERN,
            AgendaReports.ELAPSED_TIME_EXEC_PATTERN,
            AgendaReports.AGENDA_TYPE,
            AgendaReports.OPERATION_PAYLOAD
    };

    public static final String AGENDA_RESPONSE_PREFIX = "Agenda metadata - ";

    private String prefix;
    private Report<Agenda, String>[] agendaReports = DEFAULT_REPORT;

    public AgendaReporter()
    {
        this(AGENDA_RESPONSE_PREFIX, AGENDA_REPORTS);
    }

    public AgendaReporter(String prefix, Report... agendaReports)
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

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    public Logger getLogger( )
    {
        return logger;
    }
}