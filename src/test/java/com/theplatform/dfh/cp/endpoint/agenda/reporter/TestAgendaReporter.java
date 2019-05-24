package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import org.slf4j.Logger;

public class TestAgendaReporter extends AgendaReporter
{

    protected void setLogger(Logger logger)
    {
        this.logger = logger;
    }
    public TestAgendaReporter(String prefix, AgendaReports... agendaReports)
    {
        super(prefix, agendaReports);
    }
}
