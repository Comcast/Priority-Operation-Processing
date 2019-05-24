package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.theplatform.dfh.cp.endpoint.agenda.reporter.Report.CONCLUSION_STATUS_KEY;

public class AgendaResponseReporter
{
    private static Logger logger = LoggerFactory.getLogger(AgendaResponseReporter.class);

    private static final AgendaReports[] AGENDA_REPORTS = {
            AgendaReports.CID,
            AgendaReports.AGENDA_ID,
            AgendaReports.LINK_ID,
            AgendaReports.CUSTOMER_ID,
            AgendaReports.AGENDA_STATUS,
            AgendaReports.MILLISECONDS_IN_QUEUE,
            AgendaReports.AGENDA_TYPE,
            AgendaReports.OPERATION_PAYLOAD
    };


    private final GetAgendaResponse getAgendaResponse;
    private static final String AGENDA_RESPONSE_PREFIX = "Agenda-response metadata - ";

    private final AgendaReporter agendaReporter;
    private Agenda[] agendas;

    public AgendaResponseReporter(GetAgendaResponse getAgendaResponse, AgendaReporter agendaReporter)
    {
        this.getAgendaResponse = getAgendaResponse;
        this.agendas = getAgendaResponse.getAgendas() == null? new Agenda[0]: getAgendaResponse.getAgendas().toArray(new Agenda[0]);
        this.agendaReporter = agendaReporter == null ? new AgendaReporter(makeAgendaIdsPrefix(AGENDA_RESPONSE_PREFIX,agendas), AGENDA_REPORTS) : agendaReporter;;
    }

    public void reportAgendaResponse()
    {
        if(agendas.length == 0)
        {
            return;
        }
        Agenda[] agendas = getAgendaResponse.getAgendas().toArray(new Agenda[0]);
        setConclusionStatus(agendas[0]);
        agendaReporter.reportInLine(agendas[0]);
    }

    private void setConclusionStatus(Agenda agenda)
    {
        AgendaConclusionStatus status = getAgendaResponse.isError() ? AgendaConclusionStatus.failed : AgendaConclusionStatus.succeeded;
        agenda.getParams().put(CONCLUSION_STATUS_KEY, status.name());
    }

    private String makeAgendaIdsPrefix(String agendaResponsePrefix, Agenda... agendas)
    {
        StringBuilder b = new StringBuilder();
        b.append(agendaResponsePrefix);
        b.append(" Agenda IDs: [");
        for(int i = 0; i < agendas.length; i++)
        {
            b.append(AgendaReports.AGENDA_ID.report(agendas[i]));
            if(i < agendas.length - 1)
            {
                b.append("; ");
            }
        }
        b.append("] ");
        return b.toString();
    }

    public void reportAgendas()
    {
        for(Agenda agenda: agendas)
        {
            agendaReporter.reportInLine(agenda);
        }
    }
}
