package com.theplatform.dfh.cp.handler.puller.impl.client;


public class DefaultAgendaClientFactory
{
    public AgendaClient getClient()
    {
        return new DefaultAgendaClient();
    }
}
