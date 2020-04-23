package com.comcast.pop.scheduling.api;

import com.comcast.pop.object.api.IdentifiedObject;

import java.util.Date;

public interface AgendaInfo extends IdentifiedObject
{
    String getId();
    String getInsightId();
    String getAgendaId();
    String getCustomerId();
    Date getAdded();
}
