package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.HandlerContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic test/local/prototype processor for running the Agenda
 */
public class SequentialAgendaProcessor implements HandlerProcessor<Void>
{
    private static Logger logger = LoggerFactory.getLogger(KubernetesOperationExecutor.class);

    private static final String OUTPUT_SUFFIX = ".out";
    private LaunchDataWrapper launchDataWrapper;
    private HandlerContext handlerContext;
    private JsonHelper jsonHelper;

    public SequentialAgendaProcessor(LaunchDataWrapper launchDataWrapper, HandlerContext handlerContext)
    {
        this.launchDataWrapper = launchDataWrapper;
        this.handlerContext = handlerContext;
        this.jsonHelper = new JsonHelper();
    }

    /**
     * Executes the ops in the Agenda in order
     * @return
     */
    public Void execute()
    {
        ExecutorHandlerInput handlerInput;
        try
        {
            handlerInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), ExecutorHandlerInput.class);
            handlerContext.getReporter().reportProgress(handlerInput);
        }
        catch(Exception e)
        {
            throw new AgendaExecutorException("Failed to load payload.", e);
        }
        try
        {
            handlerInput.getOperations().forEach(this::executeOperation);
            handlerContext.getReporter().reportSuccess("Done!");
        }
        catch (AgendaExecutorException e)
        {
            handlerContext.getReporter().reportFailure("", e);
        }
        return null;
    }

    protected void executeOperation(Operation operation)
    {
        try
        {
            // TODO: this should return information about if all references have been resolved before continuing
            String payload = handlerContext.getJsonContext().processReferences(operation.getPayload());

            BaseOperationExecutor executor = handlerContext.getOperationExecutorFactory().getOperationExecutor(handlerContext, operation);
            String contextKey = operation.getName() + OUTPUT_SUFFIX;
            String outputPayload = executor.execute(payload);
            logger.info("Persisting ContextKey: [{}] OperationId: [{}] with OUTPUT Payload: {}", contextKey, operation.getId(), outputPayload);
            handlerContext.getJsonContext().addData(contextKey, outputPayload);
        }
        catch(Throwable t)
        {
            throw new AgendaExecutorException("Failed to execute operation.", t);
        }
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void setHandlerContext(HandlerContext handlerContext)
    {
        this.handlerContext = handlerContext;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
