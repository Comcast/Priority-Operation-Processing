package com.theplatform.dfh.cp.scheduling.api;

import java.util.Date;

public interface AgendaInfo
{
    String getInsightId();
    String getAgendaId();
    String getCustomerId();
    Date getAdded();
}
