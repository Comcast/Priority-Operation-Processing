package com.theplatform.dfh.cp.endpoint.agenda.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;

public interface AgendaFactory
{
    Agenda createAgendaFromObject(AgendaTemplate agendaTemplate, Object payload, String progressId, String cid);
}
