package com.theplatform.dfh.cp.handler.base;

import com.theplatform.dfh.cp.handler.base.context.BaseOperationContext;
import com.theplatform.dfh.cp.handler.base.context.BaseOperationContextFactory;
import com.theplatform.dfh.cp.handler.base.log.HandlerMetadataRetriever;
import com.theplatform.dfh.cp.handler.base.log.HandlerReporter;
import com.theplatform.dfh.cp.handler.base.log.HandlerReporterImpl;
import com.theplatform.dfh.cp.handler.base.processor.HandlerMetadata;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.api.args.MetaData;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static com.theplatform.dfh.cp.api.progress.CompleteStateMessage.*;
import static com.theplatform.dfh.cp.handler.base.log.HandlerReporterImpl.OPERATION_METADATA_TEMPLATE_PREFIX;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BaseHandlerEntryPoint<C extends BaseOperationContext, P extends HandlerProcessor, W extends LaunchDataWrapper>
{
    public static final String DFH_POD_TERMINATION_STRING = "DfhComplete";
    private static final String DURATION_TEMPLATE = OPERATION_METADATA_TEMPLATE_PREFIX +"operation: %s; completion status: %s; duration (millisec): %d";
    private static final String CPU_Template = OPERATION_METADATA_TEMPLATE_PREFIX +"Requested CPUs (for handler and any utility pods): %1f";
    private static final double DEFAULT_HANDLER_CPU_REQUEST = 1.0; // note that request can be fractional; confirm that this value is good as a default.
    private static Logger logger = LoggerFactory.getLogger(BaseHandlerEntryPoint.class);
    private W launchDataWrapper;
    private BaseOperationContextFactory<C> operationContextFactory;
    private HandlerMetadataRetriever handlerMetadataRetriever;
    private long start;

    protected abstract W createLaunchDataWrapper(String[] args);
    protected abstract BaseOperationContextFactory<C> createOperationContextFactory(W launchDataWrapper);
    protected abstract P createHandlerProcessor(C operationContext);

    public BaseHandlerEntryPoint(String[] args)
    {
        // gather the inputs args, environment, properties
        launchDataWrapper = createLaunchDataWrapper(args);
        // Set the CID on the thread local ASAP
        setupLoggingCid();
        logMetadata();
        operationContextFactory = createOperationContextFactory(launchDataWrapper);

    }

    private void logMetadata()
    {
        if(launchDataWrapper.getEnvironmentRetriever() == null)
        {
            logger.warn("Null environment retriever; no operation metadata logged.");
            return;
        }
        handlerMetadataRetriever = new HandlerMetadataRetriever(launchDataWrapper.getEnvironmentRetriever());
        HandlerReporter handlerReporter = new HandlerReporterImpl();
        handlerReporter.reportMetadata(handlerMetadataRetriever.getMetadata());
    }

    public void execute()
    {
        // get the operation specific context for running the overall process
        C operationContext = operationContextFactory.createOperationContext();
        Map<String, Object>  execMetaData = new HashMap<>();
        try
        {
            operationContext.init();

            start = System.currentTimeMillis();

            HandlerProcessor handlerProcessor = createHandlerProcessor(operationContext);
            if(handlerProcessor instanceof MetaData)
            {
                logger.info("Handler is of type MetaData"); // temporary logging
                execMetaData = ((MetaData)handlerProcessor).getMetadata();
                logger.info("Handler metadata pre-exec key count: " + Integer.toString(execMetaData.keySet().size())); // temporary logging
            }

            handlerProcessor.execute();
            logger.info("Handler metadata post-exec key count: " + Integer.toString(execMetaData.keySet().size())); // temporary logging
            logFinalStateAndDuration(SUCCEEDED.toString());
        }
        catch (Exception e)
        {
            logFinalStateAndDuration(FAILED.toString());
            logger.error(getOperationName(), e);
        }
        finally
        {
            logRequestedCPUs(execMetaData);
            logger.info(DFH_POD_TERMINATION_STRING);
            operationContext.shutdown();
        }
    }

    private void logRequestedCPUs(Map<String, Object> handlerMetada)
    {
        // todo If/when needed, make this logic more generic/smarter (e.g. set of metadata fields to check)

        double utilityPodCPURequest = 0;
        if(handlerMetada.keySet().contains(HandlerMetadata.RequestedCPUs.name()) && handlerMetada.get(HandlerMetadata.RequestedCPUs.name()) instanceof String)
        {
            logger.info("Handler metadata has key for requested CPUs"); // temporary logging
            Double utilityCPURequest = NumberUtils.toDouble((String) handlerMetada.get(HandlerMetadata.RequestedCPUs.name()), -1);

            if(utilityCPURequest >= 0)
            {
                utilityPodCPURequest += utilityCPURequest;
            }
        }
        logger.info(String.format(CPU_Template, DEFAULT_HANDLER_CPU_REQUEST + utilityPodCPURequest));
    }

    private boolean IsParsable(String tentativeDoubleString)
    {
        try
        {
            Double.parseDouble(tentativeDoubleString);
            return true;
        }
        catch (Exception e)
        {
            // noop
        }
        return false;
    }

    private String getOperationName()
    {
        return handlerMetadataRetriever.getMetadata().get(HandlerField.OPERATION_NAME.name());
    }

    private void logFinalStateAndDuration(String status)
    {
        long durationMilli = System.currentTimeMillis() - start;
        String operationName = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.OPERATION_ID.name());
        logger.info(String.format(DURATION_TEMPLATE, operationName, status, durationMilli));
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
