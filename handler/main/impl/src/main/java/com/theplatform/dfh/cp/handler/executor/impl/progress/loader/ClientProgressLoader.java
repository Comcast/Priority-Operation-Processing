package com.theplatform.dfh.cp.handler.executor.impl.progress.loader;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;

public class ClientProgressLoader implements ProgressLoader
{
    private ObjectClient<AgendaProgress> agendaProgressClient;

    public ClientProgressLoader()
    {

    }

    public ClientProgressLoader(ObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    @Override
    public AgendaProgress loadProgress(String agendaProgressId)
    {
        DataObjectResponse<AgendaProgress> response = agendaProgressClient.getObject(agendaProgressId);
        if(response.isError())
        {

        }
        else if(response.getCount() == 0)
        {

        }
        else
        {
            return response.getFirst();
        }
        return null;
    }
}
