package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaResponseReporter
{
    private static Logger logger = LoggerFactory.getLogger(AgendaResponseReporter.class);

    private final GetAgendaResponse getAgendaResponse;
    private static final String AGENDA_RESPONSE_PREFIX = "Agenda-response metadata - ";

    private AgendaReporter agendaReporter;
    private Agenda[] agendas;

    public AgendaResponseReporter(GetAgendaResponse getAgendaResponse, AgendaReporter agendaReporter)
    {
        this.getAgendaResponse = getAgendaResponse;
        this.agendas = getAgendaResponse.getAgendas() == null? new Agenda[0]: getAgendaResponse.getAgendas().toArray(new Agenda[0]);
        this.agendaReporter = agendaReporter;
    }


    public  void reportAgendaResponse()
    {
        if(agendas.length == 0)
        {
            return;
        }
        AgendaReporter reporter = new AgendaReporter(AGENDA_RESPONSE_PREFIX, AgendaReports.CID, AgendaReports.CUSTOMER_ID);
        logger.info(AGENDA_RESPONSE_PREFIX, "agendaType: basic"); // todo implement agenda types when spec'ed
        reporter.report(getAgendaResponse.getAgendas().toArray(new Agenda[0])[0]); // use first agenda to report CID and customer id, if they are provided.
    }

    public void reportAgendas()
    {
        for(Agenda agenda: agendas)
        {
            agendaReporter.report(agenda);
        }
    }

}
