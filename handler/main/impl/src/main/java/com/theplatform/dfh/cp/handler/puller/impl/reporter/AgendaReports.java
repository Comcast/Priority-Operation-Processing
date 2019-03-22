package com.theplatform.dfh.cp.handler.puller.impl.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import org.apache.commons.lang3.StringUtils;

public enum AgendaReports implements AgendaReport
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
                    return cid;
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
                    return customerId;
                }
            },
    AGENDA_ID
            {
                @Override
                public String report(Agenda agenda)
                {
                    return agenda.getId();
                }
            };
}
