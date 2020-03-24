package com.theplatform.dfh.cp.scheduling.api;

import com.theplatform.dfh.object.api.IdentifiedObject;

import java.util.Date;

public interface AgendaInfo extends IdentifiedObject
{
    String getId();
    String getInsightId();
    String getAgendaId();
    String getCustomerId();
    Date getAdded();
}
