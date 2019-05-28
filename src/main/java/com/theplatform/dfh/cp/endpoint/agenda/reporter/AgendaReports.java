package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

public enum AgendaReports implements Report<AgendaReportData, String>
{
    CID("cid")
            {
                @Override
                public String report(AgendaReportData agendaData)
                {
                    Agenda agenda = agendaData.getAgenda();
                    String cid = "No CID defined for: "+agenda.getId();
                    String agendaCid = agenda.getCid();
                    if(!StringUtils.isEmpty(agendaCid))
                    {
                        cid = agendaCid;
                    }
                    return label + ": " + cid;
                }
            },

    CUSTOMER_ID("owner")
            {
                @Override
                public String report(AgendaReportData agendaData)
                {
                    Agenda agenda = agendaData.getAgenda();
                    String customerId = "No Customer ID defined for: "+agenda.getId();
                    String agendaCustomerId = agenda.getCustomerId();
                    if(!StringUtils.isEmpty(agendaCustomerId))
                    {
                        customerId = agendaCustomerId;
                    }
                    return label + ": " + customerId;
                }
            },
    AGENDA_ID("agendaId")
            {
                @Override
                public String report(AgendaReportData agendaData)
                {
                    return label + ": " + agendaData.getAgenda().getId();
                }
            },
    MILLISECONDS_IN_QUEUE("elapsedTime")
            {
                @Override
                public String report(AgendaReportData agendaData)
                {
                    Agenda agenda = agendaData.getAgenda();
                    if(agenda.getParams() == null || !agenda.getParams().keySet().contains(ADDED_KEY) || agenda.getParams().get(ADDED_KEY) == null || !(agenda.getParams().get(ADDED_KEY) instanceof Date))
                    {
                        return label +": No duration recorded";
                    }
                    Date added = (Date)agenda.getParams().get(ADDED_KEY);
                    Long durationMilli = System.currentTimeMillis() - added.getTime();
                    return label +": " +durationMilli.toString();
                }
            },
    AGENDA_STATUS("conclusionStatus")
            {
                @Override
                public String report(AgendaReportData agendaData)
                {
                    if(agendaData.getAgendaState() == null )
                    {
                        return label +": No conclusion status recorded";
                    }
                    return label +": " + agendaData.getAgendaState();
                }
            },
    LINK_ID("linkId")
            {
                @Override
                public String report(AgendaReportData agendaData)
                {
                    Agenda agenda = agendaData.getAgenda();
                    String linkId = "No link ID defined for: "+agenda.getLinkId();
                    String agendaLinkId = agenda.getLinkId();
                    if(!StringUtils.isEmpty(agendaLinkId))
                    {
                        linkId = agendaLinkId;
                    }
                    return label + ": " + linkId;
                }
            },
    AGENDA_TYPE("agendaType")
            {
                @Override
                public String report(AgendaReportData agendaData) // TODO support when agenda type is implemented
                {
                    return label + ": " + "basic";
                }
            },
    OPERATION_PAYLOAD("payload")
            {
                @Override
                public String report(AgendaReportData agendaData)
                {
                    Agenda agenda = agendaData.getAgenda();
                    List<Operation> operations = agenda.getOperations();
                    if(operations == null || operations.size() == 0)
                    {
                        return label + ": No operations";
                    }
                    JsonHelper jsonHelper = new JsonHelper();
                    jsonHelper.getJSONString(agenda.getOperations());
                        return label + ": " + jsonHelper.getJSONString(agenda.getOperations());
                }
            };

    protected final String label;

    AgendaReports(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
