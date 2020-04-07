package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.operation.Operation;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

public enum AgendaReports implements Report<Agenda, String>
{
    CID("cid")
            {
                @Override
                public String report(Agenda agenda)
                {
                    String cid = "No CID defined for: "+agenda.getId();
                    String agendaCid = agenda.getCid();
                    if(!StringUtils.isEmpty(agendaCid))
                    {
                        cid = agendaCid;
                    }
                    return label + "=" + cid;
                }
            },

    CUSTOMER_ID("owner")
            {
                @Override
                public String report(Agenda agenda)
                {
                    String customerId = "No Customer ID defined for: "+agenda.getId();
                    String agendaCustomerId = agenda.getCustomerId();
                    if(!StringUtils.isEmpty(agendaCustomerId))
                    {
                        customerId = agendaCustomerId;
                    }
                    return label + "=" + customerId;
                }
            },
    AGENDA_ID("agendaId")
            {
                @Override
                public String report(Agenda agenda)
                {
                    return label + "=" + agenda.getId();
                }
            },
    MILLISECONDS_TOTAL("totalElapsedTime")
            {
                @Override
                public String report(Agenda agenda)
                {
                    if(agenda.getParams() == null || !agenda.getParams().keySet().contains(ADDED_KEY) || agenda.getParams().get(ADDED_KEY) == null || !(agenda.getParams().get(ADDED_KEY) instanceof Date))
                    {
                        return label +"=No duration recorded";
                    }
                    Date added = (Date)agenda.getParams().get(ADDED_KEY);
                    Long durationMilli = System.currentTimeMillis() - added.getTime();
                    return label +"=" +durationMilli.toString();
                }
            },
    ELAPSED_TIME_EXEC_PATTERN("elapsedTime")
            {
                @Override
                public String report(Agenda agenda)
                {
                    return getPattern();
                }
            },
    AGENDA_STATUS_PATTERN("conclusionStatus")
            {
                @Override
                public String report(Agenda agenda)
                {
                    return getPattern();
                }
            },
    LINK_ID("linkId")
            {
                @Override
                public String report(Agenda agenda)
                {
                    String linkId = "No link ID defined for: "+agenda.getLinkId();
                    String agendaLinkId = agenda.getLinkId();
                    if(!StringUtils.isEmpty(agendaLinkId))
                    {
                        linkId = agendaLinkId;
                    }
                    return label + "=" + linkId;
                }
            },
    AGENDA_TYPE("agendaType")
            {
                @Override
                public String report(Agenda agenda) // TODO support when agenda type is implemented
                {
                    return label + "=" + "basic";
                }
            },
    OPERATION_PAYLOAD("payload")
            {
                @Override
                public String report(Agenda agenda)
                {
                    List<Operation> operations = agenda.getOperations();
                    if(operations == null || operations.size() == 0)
                    {
                        return label + ": No operations";
                    }
                    JsonHelper jsonHelper = new JsonHelper();
                    jsonHelper.getJSONString(agenda.getOperations());
                        return label + "=" + jsonHelper.getJSONString(agenda.getOperations());
                }
            },
    OPERATION_PAYLOAD_PATTERN("payload")
            {
                @Override
                public String report(Agenda agenda)
                {
                    return getPattern();
                }
            },;

    protected final String label;

    public String getPattern()
    {
        return label + "=%s";
    }

    AgendaReports(String label)
    {
        this.label = label;
    }

    public String getLabel()
    {
        return label;
    }
}
