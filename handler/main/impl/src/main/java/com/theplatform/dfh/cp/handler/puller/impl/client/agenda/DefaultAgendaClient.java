package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * Basic client for making calls to get Agenda objects.  Eventually this will
 * talk to the API Gateway to getWork, but for now it returns a static Agenda
 */
public class DefaultAgendaClient implements AgendaClient
{
    private String payloadFileName = "/EncodeAgenda2.json";
    private JsonHelper jsonHelper = new JsonHelper();

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


    public Agenda getAgenda()
    {
        return jsonHelper.getObjectFromString(work, Agenda.class);
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
            this.getClass().getResource(file),
            "UTF-8"
        );
    }
}
