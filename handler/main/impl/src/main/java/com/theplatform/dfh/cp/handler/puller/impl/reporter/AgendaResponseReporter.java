package com.theplatform.dfh.cp.handler.puller.impl.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgendaResponseReporter
{
    private static Logger logger = LoggerFactory.getLogger(AgendaResponseReporter.class);

    private final GetAgendaResponse getAgendaResponse;
    private static final String PULLER_AGENDA_RESPONSE_PREFIX = "Puller agenda-response metadata - ";

    private AgendaReporter agendaReporter;

    public AgendaResponseReporter(GetAgendaResponse getAgendaResponse, AgendaReporter agendaReporter)
    {
        this.getAgendaResponse = getAgendaResponse;
        this.agendaReporter = agendaReporter;
    }


    public  void reportAgendaResponse()
    {
        AgendaReporter reporter = new AgendaReporter(PULLER_AGENDA_RESPONSE_PREFIX, AgendaReports.CID, AgendaReports.CUSTOMER_ID);
        logger.info(PULLER_AGENDA_RESPONSE_PREFIX, "Agenda type: basic"); // todo implement agenda types when needed
        reporter.report(getAgendaResponse.getAgendas().toArray(new Agenda[0])[0]); // use first agenda to report CID and customer id, if they are provided.
    }

    public void reportAgendas()
    {
        Agenda[] agendas = getAgendaResponse.getAgendas().toArray(new Agenda[0]);
        int length = agendas.length;
        for(Agenda agenda: agendas)
        {
                agendaReporter.report(agenda);
        }
    }
}
