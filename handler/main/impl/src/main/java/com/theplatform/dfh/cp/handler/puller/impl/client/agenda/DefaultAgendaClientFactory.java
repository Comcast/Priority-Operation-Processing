package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;


public class DefaultAgendaClientFactory implements AgendaClientFactory
{
    String payloadFileName = "/EncodeAgenda2.json";

    public DefaultAgendaClientFactory(String payloadFileName)
    {
        this.payloadFileName = payloadFileName;
    }

    public DefaultAgendaClientFactory(){};

    public DefaultAgendaClient getClient()
    {
        return new DefaultAgendaClient(payloadFileName);
    }
}
