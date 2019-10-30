package com.theplatform.dfh.cp.handler.puller.impl.client.agenda;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaRequest;
import com.theplatform.dfh.endpoint.api.resourcepool.service.GetAgendaResponse;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Local testing to use an Agenda stored in a json file.
 */
public class LocalResourcePoolServiceClient extends ResourcePoolServiceClient
{
    private JsonHelper jsonHelper = new JsonHelper();
    // Do not flood the puller (it will keep retrieving as fast as possible)
    private final int MAX_RESPONSES = 1;
    private int agendaResponseCount = 0;

    private String work;

    public LocalResourcePoolServiceClient(String payloadFileName)
    {
        super("UnusedValue", null);
        try
        {
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
            return IOUtils.toString(this.getClass().getResource(file), "UTF-8");
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
        List<Agenda> agendaList = new ArrayList<>();
        if(agendaResponseCount < MAX_RESPONSES)
        {
            agendaList.add(getAgenda());
        }
        getAgendaResponse.setAll(agendaList);
        agendaResponseCount += agendaList.size();
        return getAgendaResponse;
    }
}
