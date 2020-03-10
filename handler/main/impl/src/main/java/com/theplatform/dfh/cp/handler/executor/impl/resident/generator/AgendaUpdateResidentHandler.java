package com.theplatform.dfh.cp.handler.executor.impl.resident.generator;

import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.handler.base.progress.OperationProgressFactory;
import com.theplatform.dfh.cp.handler.base.resident.BaseResidentHandler;
import com.theplatform.dfh.cp.handler.executor.impl.config.ExecutorConfigProperty;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.processor.operation.generator.ResourcePoolAgendaUpdater;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.agenda.service.ExpandAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.ExpandAgendaResponse;
import com.theplatform.dfh.endpoint.client.ResourcePoolServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation Generator Resident handler - Creates operations on the persisted Agenda/Progress using the inputs provided (operations/params)
 *
 * This operation should be coupled with a param at the operation level (to trigger executor to introduce the same ops): agenda.operation.generator
 *
 * TODO: consider other alternatives so the executor can be told a given operation creates other ops
 */
public class AgendaUpdateResidentHandler extends BaseResidentHandler<AgendaUpdateHandlerInput, OperationProgressFactory>
{
    private static final int DEFAULT_EXPAND_AGENDA_ATTEMPTS = 3;
    private static final int DEFAULT_EXPAND_AGENDA_ATTEMPT_DELAY_MS = 3000;
    private static final Logger logger = LoggerFactory.getLogger(AgendaUpdateResidentHandler.class);
    private ResourcePoolAgendaUpdater resourcePoolAgendaUpdater;
    private ExecutorContext executorContext;

    public AgendaUpdateResidentHandler(ExecutorContext executorContext)
    {
        this.executorContext = executorContext;
        this.resourcePoolAgendaUpdater = new ResourcePoolAgendaUpdater();
        PropertyRetriever propertyRetriever = executorContext.getLaunchDataWrapper().getPropertyRetriever();
        resourcePoolAgendaUpdater.setMaxRetries(
            propertyRetriever.getInt(ExecutorConfigProperty.EXPAND_AGENDA_ATTEMPTS, DEFAULT_EXPAND_AGENDA_ATTEMPTS) - 1);
        resourcePoolAgendaUpdater.setRetryDelayMs(
            propertyRetriever.getInt(ExecutorConfigProperty.EXPAND_AGENDA_DELAY_MS, DEFAULT_EXPAND_AGENDA_ATTEMPT_DELAY_MS));
    }

    @Override
    public String execute(AgendaUpdateHandlerInput handlerInput)
    {
        AgendaUpdateHandlerOutput agendaUpdateHandlerOutput = new AgendaUpdateHandlerOutput();
        agendaUpdateHandlerOutput.setOperations(handlerInput.getOperations());
        agendaUpdateHandlerOutput.setParams(handlerInput.getParams());
        updatePersistedAgenda(handlerInput);
        getProgressReporter().reportProgress(getOperationProgressFactory().create(ProcessingState.COMPLETE, CompleteStateMessage.SUCCEEDED.toString()));
        return new JsonHelper().getJSONString(agendaUpdateHandlerOutput);
    }

    @Override
    public OperationProgressFactory getOperationProgressFactory()
    {
        return new OperationProgressFactory();
    }

    @Override
    public Class<AgendaUpdateHandlerInput> getPayloadClassType()
    {
        return AgendaUpdateHandlerInput.class;
    }

    protected void updatePersistedAgenda(AgendaUpdateHandlerInput handlerInput)
    {
        ExpandAgendaRequest expandAgendaRequest = new ExpandAgendaRequest();
        expandAgendaRequest.setAgendaId(executorContext.getAgendaId());
        expandAgendaRequest.setOperations(handlerInput.getOperations());
        expandAgendaRequest.setParams(handlerInput.getParams());

        if(handlerInput.getLogOnly() != null && handlerInput.getLogOnly())
        {
            logger.info("Skipping agendaUpdate call (by request): {}", executorContext.getJsonHelper().getJSONString(expandAgendaRequest));
            return;
        }

        ResourcePoolServiceClient resourcePoolServiceClient = executorContext.getResourcePoolServiceClient();
        if(null == resourcePoolServiceClient)
        {
            logger.info("No ResourcePoolServiceClient defined. Generated operations are not being persisted.");
            return;
        }
        ExpandAgendaResponse response = resourcePoolAgendaUpdater.update(resourcePoolServiceClient, expandAgendaRequest);
        if(response == null)
            throw new RuntimeException("Failed to persist Agenda with generated operations.");
        if(response.isError())
            throw new RuntimeException(String.format("Failed to persist Agenda with generated operations. %1$s",
                response.getErrorResponse().toString()));
    }

    public void setResourcePoolAgendaUpdater(ResourcePoolAgendaUpdater resourcePoolAgendaUpdater)
    {
        this.resourcePoolAgendaUpdater = resourcePoolAgendaUpdater;
    }
}

