package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;


public class DefaultAgendaClientFactory implements AgendaClientFactory
{
    public DefaultAgendaClient getClient()
    {
        return new DefaultAgendaClient();
    }
}
