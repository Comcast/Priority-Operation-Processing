package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

public enum AgendaReports implements Report<Agenda, String>
{
    CID
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
                    return name() + ": " + cid;
                }
            },

    CUSTOMER_ID
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
                    return name() + ": " + customerId;
                }
            },
    AGENDA_ID
            {
                @Override
                public String report(Agenda agenda)
                {
                    return name() + ": " + agenda.getId();
                }
            },
    MILLISECONDS_IN_QUEUE
            {
                @Override
                public String report(Agenda agenda)
                {
                    if(agenda.getParams() == null || !agenda.getParams().keySet().contains(ADDED_KEY) || agenda.getParams().get(ADDED_KEY) == null || !(agenda.getParams().get(ADDED_KEY) instanceof Date))
                    {
                        return name() +": No duration recorded";
                    }
                    Date added = (Date)agenda.getParams().get(ADDED_KEY);
                    Long durationMilli = System.currentTimeMillis() - added.getTime();
                    return name() +": " +durationMilli.toString();
                }
            },
    LINK_ID
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
                    return name() + ": " + linkId;
                }
            },
    AGENDA_TYPE
            {
                @Override
                public String report(Agenda agenda) // TODO support when agenda type is implemented
                {
                    return name() + ": " + "basic";
                }
            },
    OPERATION_PAYLOAD
            {
                @Override
                public String report(Agenda agenda)
                {
                    List<Operation> operations = agenda.getOperations();
                    if(operations == null || operations.size() == 0)
                    {
                        return name() + ": No operations";
                    }
                    JsonHelper jsonHelper = new JsonHelper();
                    jsonHelper.getJSONString(agenda.getOperations());
                        return name() + ": " + jsonHelper.getJSONString(agenda.getOperations());
                }
            };

}
