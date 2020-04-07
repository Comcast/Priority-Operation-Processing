package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.comcast.pop.api.Agenda;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.slf4j.Logger;

public class TestAgendaProgressReporter extends AgendaProgressReporter
{
    protected void setLogger(Logger logger)
    {
        this.logger = logger;
    }
    public TestAgendaProgressReporter(ObjectPersister<Agenda> agendaPersister)
    {
        super(agendaPersister);
    }

}
