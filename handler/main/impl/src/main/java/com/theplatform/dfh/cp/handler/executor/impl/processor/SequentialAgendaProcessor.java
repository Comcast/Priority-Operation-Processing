package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.ReferenceReplacementResult;
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
    private ExecutorContext executorContext;
    private JsonHelper jsonHelper;

    public SequentialAgendaProcessor(LaunchDataWrapper launchDataWrapper, ExecutorContext executorContext)
    {
        this.launchDataWrapper = launchDataWrapper;
        this.executorContext = executorContext;
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
            executorContext.getReporter().reportProgress(handlerInput);
        }
        catch(Exception e)
        {
            throw new AgendaExecutorException("Failed to load payload.", e);
        }
        try
        {
            handlerInput.getOperations().forEach(this::executeOperation);
            executorContext.getReporter().reportSuccess("Done!");
        }
        catch (AgendaExecutorException e)
        {
            executorContext.getReporter().reportFailure("", e);
            logger.error("", e);
        }
        logger.info("ExecutorComplete");
        return null;
    }

    protected void executeOperation(Operation operation)
    {
        try
        {
            // TODO: Use the information about missing/invalid references.
            ReferenceReplacementResult result = executorContext.getJsonContext().processReferences(operation.getPayload());

            BaseOperationExecutor executor = executorContext.getOperationExecutorFactory().createOperationExecutor(executorContext, operation);
            String contextKey = operation.getName() + OUTPUT_SUFFIX;
            String outputPayload = executor.execute(result.getResult());
            logger.info("Persisting ContextKey: [{}] OperationId: [{}] with OUTPUT Payload: {}", contextKey, operation.getId(), outputPayload);
            executorContext.getJsonContext().addData(contextKey, outputPayload);
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

    public void setExecutorContext(ExecutorContext executorContext)
    {
        this.executorContext = executorContext;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
