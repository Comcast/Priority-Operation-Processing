package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;

/**
 * Note: This was named this way to not have everything named Operation
 */
public class ExecutorContext extends BaseOperationContext
{
    private OperationExecutorFactory operationExecutorFactory;
    private JsonContext jsonContext;

    public ExecutorContext(Reporter reporter, LaunchDataWrapper launchDataWrapper, OperationExecutorFactory operationExecutorFactory)
    {
        super(reporter, launchDataWrapper);
        this.operationExecutorFactory = operationExecutorFactory;
        this.jsonContext = new JsonContext();
    }

    public OperationExecutorFactory getOperationExecutorFactory()
    {
        return operationExecutorFactory;
    }

    public void setOperationExecutorFactory(OperationExecutorFactory operationExecutorFactory)
    {
        this.operationExecutorFactory = operationExecutorFactory;
    }

    public JsonContext getJsonContext()
    {
        return jsonContext;
    }

    public void setJsonContext(JsonContext jsonContext)
    {
        this.jsonContext = jsonContext;
    }
}
