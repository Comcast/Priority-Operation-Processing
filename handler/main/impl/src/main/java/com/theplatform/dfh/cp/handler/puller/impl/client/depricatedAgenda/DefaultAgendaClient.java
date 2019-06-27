package com.theplatform.dfh.cp.handler.puller.impl.client.depricatedAgenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic client for making calls to get Agenda objects.  Eventually this will
 * talk to the API Gateway to getWork, but for now it returns a static Agenda
 */
public class DefaultAgendaClient implements AgendaClient
{
    private String payloadFileName;
    private JsonHelper jsonHelper = new JsonHelper();

    private String work;

    private DefaultAgendaClient()
    {
    }

    public DefaultAgendaClient(String payloadFileName)
    {
        this.payloadFileName = payloadFileName;
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
        if (this.getClass().getResource(file) != null)
        {
            return IOUtils.toString(
                    this.getClass().getResource(file),
                    "UTF-8"
            );
        }
        else
        {
            throw new IOException("File [" + file + "] doesn't exist");
        }
    }

    @Override
    public GetAgendaResponse getAgenda(GetAgendaRequest getAgendaRequest)
    {
        GetAgendaResponse getAgendaResponse = new GetAgendaResponse();
        Agenda agenda = getAgenda();
        List<Agenda> agendaList = new ArrayList<>();
        agendaList.add(agenda);
        getAgendaResponse.setAgendas(agendaList);
        return getAgendaResponse;
    }
}
