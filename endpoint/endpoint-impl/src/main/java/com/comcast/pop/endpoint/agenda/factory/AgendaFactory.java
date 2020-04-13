package com.comcast.pop.endpoint.agenda.factory;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;

public interface AgendaFactory
{
    Agenda createAgendaFromObject(AgendaTemplate agendaTemplate, Object payload, String progressId, String cid);
}
