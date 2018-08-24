package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;


public class DefaultAgendaClientFactory
{
    public AgendaClient getClient()
    {
        return new DefaultAgendaClient();
    }
}
