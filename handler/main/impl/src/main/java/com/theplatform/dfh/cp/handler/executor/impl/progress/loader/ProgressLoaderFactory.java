package com.theplatform.dfh.cp.handler.executor.impl.progress.loader;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.apache.commons.lang3.StringUtils;

public class ProgressLoaderFactory
{
    public ProgressLoader createProgressLoader(ExecutorContext executorContext)
    {
        LaunchDataWrapper launchDataWrapper = executorContext.getLaunchDataWrapper();
        if(launchDataWrapper == null || launchDataWrapper.getPropertyRetriever() == null) return null;
        // if arg is present use file loader (local dev generally)

        // if arg is not present create a client progress loader
        String agendaProgressEndpoint = launchDataWrapper.getPropertyRetriever().getField("agenda.progress.endpoint.url", null);
        if(StringUtils.isNotBlank(agendaProgressEndpoint))
        {
            ObjectClient<AgendaProgress> client =
                new HttpObjectClient<>(agendaProgressEndpoint, executorContext.getUrlConnectionFactory(), AgendaProgress.class);
            return new ClientProgressLoader(client);
        }
        return null;
    }
}
