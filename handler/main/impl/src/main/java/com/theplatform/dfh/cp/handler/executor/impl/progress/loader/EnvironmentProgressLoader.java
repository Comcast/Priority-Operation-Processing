package com.theplatform.dfh.cp.handler.executor.impl.progress.loader;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentProgressLoader extends ProgressLoader
{
    private static Logger logger = LoggerFactory.getLogger(EnvironmentProgressLoader.class);

    public EnvironmentProgressLoader(ExecutorContext executorContext)
    {
        super(executorContext);
    }

    @Override
    public AgendaProgress loadProgress()
    {
        EnvironmentFieldRetriever fieldRetriever = getExecutorContext().getLaunchDataWrapper().getEnvironmentRetriever();
        String progressJson = fieldRetriever.getField(HandlerField.LAST_PROGRESS.name(), null);
        return parseAgendaProgress(progressJson);
    }
}
