package com.theplatform.dfh.cp.endpoint.agenda.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;

public interface AgendaFactory
{
    Agenda createAgenda(TransformRequest transformRequest, String progressId, String cid);
}
