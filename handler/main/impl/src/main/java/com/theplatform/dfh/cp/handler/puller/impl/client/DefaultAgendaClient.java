package com.theplatform.dfh.cp.handler.puller.impl.client;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Basic client for making calls to get Agenda objects.  Eventually this will
 * talk to the API Gateway to getWork, but for now it returns a static Agenda
 */
public class DefaultAgendaClient implements AgendaClient
{
    private String payloadFileName = "/Agenda.json";

    private String work;

    public DefaultAgendaClient()
    {
        try {
            work = getStringFromResourceFile(payloadFileName);
        }
        catch(IOException e)
        {
            throw new RuntimeException(String.format("Failed to load payload from: %1$s", payloadFileName), e);
        }
    }


    public String getAgenda()
    {
        return work;
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
            this.getClass().getResource(file),
            "UTF-8"
        );
    }
}
