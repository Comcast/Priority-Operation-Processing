package com.theplatform.dfh.cp.handler.executor.impl.context;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.executor.impl.executor.factory.OperationExecutorFactory;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonContext;

public class HandlerContext extends BaseOperationContext
{
    private OperationExecutorFactory operationExecutorFactory;
    private LaunchDataWrapper launchDataWrapper;
    private JsonContext jsonContext;

    public HandlerContext(Reporter reporter, LaunchDataWrapper launchDataWrapper, OperationExecutorFactory operationExecutorFactory)
    {
        super(reporter);
        this.operationExecutorFactory = operationExecutorFactory;
        this.jsonContext = new JsonContext();
        this.launchDataWrapper = launchDataWrapper;
    }

    public OperationExecutorFactory getOperationExecutorFactory()
    {
        return operationExecutorFactory;
    }

    public void setOperationExecutorFactory(OperationExecutorFactory operationExecutorFactory)
    {
        this.operationExecutorFactory = operationExecutorFactory;
    }

    public LaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
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
