package com.comcast.pop.handler.executor.impl.resident.generator;

import com.comcast.pop.handler.executor.impl.processor.operation.generator.ResourcePoolAgendaUpdater;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.ProcessingState;
import com.comast.pop.handler.base.field.retriever.properties.PropertyRetriever;
import com.comast.pop.handler.base.progress.OperationProgressFactory;
import com.comast.pop.handler.base.resident.BaseResidentHandler;
import com.comcast.pop.handler.executor.impl.config.ExecutorConfigProperty;
import com.comcast.pop.handler.executor.impl.context.ExecutorContext;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.UpdateAgendaResponse;
import com.comcast.pop.endpoint.client.ResourcePoolServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Operation Generator Resident handler - Creates operations on the persisted Agenda/Progress using the inputs provided (operations/params)
 *
 * This operation should be coupled with a param at the operation level (to trigger executor to introduce the same ops): agenda.operation.generator
 *
 * TODO: consider other alternatives so the executor can be told a given operation creates other ops
 */
public class UpdateAgendaResidentHandler extends BaseResidentHandler<UpdateAgendaHandlerInput, OperationProgressFactory>
{
    private static final int DEFAULT_EXPAND_AGENDA_ATTEMPTS = 3;
    private static final int DEFAULT_EXPAND_AGENDA_ATTEMPT_DELAY_MS = 3000;
    private static final Logger logger = LoggerFactory.getLogger(UpdateAgendaResidentHandler.class);
    private ResourcePoolAgendaUpdater resourcePoolAgendaUpdater;
    private ExecutorContext executorContext;

    public UpdateAgendaResidentHandler(ExecutorContext executorContext)
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
    public String execute(UpdateAgendaHandlerInput handlerInput)
    {
        UpdateAgendaHandlerOutput updateAgendaHandlerOutput = new UpdateAgendaHandlerOutput();
        updateAgendaHandlerOutput.setOperations(processOperations(handlerInput.getOperations()));
        updateAgendaHandlerOutput.setParams(handlerInput.getParams());
        updatePersistedAgenda(handlerInput);
        getProgressReporter().reportProgress(getOperationProgressFactory().create(ProcessingState.COMPLETE, CompleteStateMessage.SUCCEEDED.toString()));
        return new JsonHelper().getJSONString(updateAgendaHandlerOutput);
    }

    @Override
    public OperationProgressFactory getOperationProgressFactory()
    {
        return new OperationProgressFactory();
    }

    @Override
    public Class<UpdateAgendaHandlerInput> getPayloadClassType()
    {
        return UpdateAgendaHandlerInput.class;
    }

    private List<Operation> processOperations(List<Operation> operations)
    {
        final String generatorOpName = getResidentHandlerParams().getOperation().getName();
        operations.forEach(op ->
        {
            if(op.getParams() == null)
                op.setParams(new ParamsMap());
            op.getParams().put(GeneralParamKey.generatedOperationParent, generatorOpName);
        });
        return operations;
    }

    protected void updatePersistedAgenda(UpdateAgendaHandlerInput handlerInput)
    {
        UpdateAgendaRequest updateAgendaRequest = new UpdateAgendaRequest();
        updateAgendaRequest.setAgendaId(executorContext.getAgendaId());
        updateAgendaRequest.setOperations(handlerInput.getOperations());
        updateAgendaRequest.setParams(handlerInput.getParams());

        if(handlerInput.getLogOnly() != null && handlerInput.getLogOnly())
        {
            logger.info("Skipping agendaUpdate call (by request): {}", executorContext.getJsonHelper().getJSONString(updateAgendaRequest));
            return;
        }

        ResourcePoolServiceClient resourcePoolServiceClient = executorContext.getResourcePoolServiceClient();
        if(null == resourcePoolServiceClient)
        {
            logger.info("No ResourcePoolServiceClient defined. Generated operations are not being persisted.");
            return;
        }
        UpdateAgendaResponse response = resourcePoolAgendaUpdater.update(resourcePoolServiceClient, updateAgendaRequest);
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

