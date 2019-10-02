package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;


import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;

public interface AgendaClient
{
    GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest);
}
