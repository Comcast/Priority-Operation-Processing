package com.theplatform.dfh.cp.handler.base;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.UUID;

public abstract class BaseHandlerEntryPoint<C extends BaseOperationContext, P extends HandlerProcessor, W extends LaunchDataWrapper>
{
    public static final String DFH_POD_TERMINATION_STRING = "DfhComplete";
    private static Logger logger = LoggerFactory.getLogger(BaseHandlerEntryPoint.class);
    private W launchDataWrapper;
    private BaseOperationContextFactory<C> operationContextFactory;

    protected abstract W createLaunchDataWrapper(String[] args);
    protected abstract BaseOperationContextFactory<C> createOperationContextFactory(W launchDataWrapper);
    protected abstract P createHandlerProcessor(C operationContext);

    public BaseHandlerEntryPoint(String[] args)
    {
        // gather the inputs args, environment, properties
        launchDataWrapper = createLaunchDataWrapper(args);
        // Set the CID on the thread local ASAP
        setupLoggingCid();
        operationContextFactory = createOperationContextFactory(launchDataWrapper);
    }

    public void execute()
    {
        // get the operation specific context for running the overall process
        C operationContext = operationContextFactory.createOperationContext();
        try
        {
            operationContext.init();
            createHandlerProcessor(operationContext).execute();
        }
        finally
        {
            logger.info(DFH_POD_TERMINATION_STRING);
            operationContext.shutdown();
        }
    }

    public W getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(W launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public BaseOperationContextFactory<C> getOperationContextFactory()
    {
        return operationContextFactory;
    }

    public void setOperationContextFactory(BaseOperationContextFactory<C> operationContextFactory)
    {
        this.operationContextFactory = operationContextFactory;
    }

    /**
     * Default CID setup assumes it comes from the CID environment variable. At worst a cid is generated.
     */
    protected void setupLoggingCid()
    {
        FieldRetriever fieldRetriever = launchDataWrapper.getEnvironmentRetriever();
        String cid = fieldRetriever != null
                     ? fieldRetriever.getField(HandlerField.CID.name(), UUID.randomUUID().toString())
                     : UUID.randomUUID().toString();
        MDC.put(HandlerField.CID.name(), cid);
    }
}
