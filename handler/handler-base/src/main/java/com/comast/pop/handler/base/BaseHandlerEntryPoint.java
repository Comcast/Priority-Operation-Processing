package com.comast.pop.handler.base;

import com.comast.pop.handler.base.context.BaseOperationContext;
import com.comast.pop.handler.base.context.BaseOperationContextFactory;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.api.args.MetaData;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.comast.pop.handler.base.log.HandlerMetadataRetriever;
import com.comast.pop.handler.base.log.HandlerReporter;
import com.comast.pop.handler.base.log.HandlerReporterImpl;
import com.comast.pop.handler.base.messages.HandlerMessages;
import com.comast.pop.handler.base.processor.HandlerMetadata;
import com.comast.pop.handler.base.processor.HandlerProcessor;
import com.comcast.pop.version.info.ServiceBuildPropertiesContainer;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static com.comast.pop.handler.base.log.HandlerReporterImpl.OPERATION_METADATA_TEMPLATE_PREFIX;
import static com.comcast.pop.api.progress.CompleteStateMessage.FAILED;
import static com.comcast.pop.api.progress.CompleteStateMessage.SUCCEEDED;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class BaseHandlerEntryPoint<C extends BaseOperationContext, P extends HandlerProcessor, W extends LaunchDataWrapper>
{
    public static final String POD_TERMINATION_STRING = "HandlerComplete";
    private static final String DURATION_TEMPLATE = OPERATION_METADATA_TEMPLATE_PREFIX +"operation: %s; completion status: %s; duration (millisec): %d";
    private static final String OPERATION_METRICS_TEMPLATE = "cid=%s operationId=%s owner=%s conclusionStatus=%s elapsedTime=%s operation=%s payload=%s";
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
        // log the plbuild properties file (ignoring errors)
        ServiceBuildPropertiesContainer.logServiceBuildString(logger, false);
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
        C operationContext = operationContextFactory.create();
        Map<String, Object>  execMetaData = new HashMap<>();
        try
        {
            operationContext.init();

            start = System.currentTimeMillis();

            P handlerProcessor = createHandlerProcessor(operationContext);
            if(handlerProcessor instanceof MetaData)
            {
                execMetaData = ((MetaData)handlerProcessor).getMetadata();
            }

            handlerProcessor.execute();
            logFinalStateAndDuration(SUCCEEDED.toString());
        }
        catch (Exception e)
        {
            logFinalStateAndDuration(FAILED.toString());
            logger.error(getOperationName(), e);
            operationContext.processUnhandledException(HandlerMessages.GENERAL_HANDLER_ERROR.getMessage(getOperationName()), e);
        }
        finally
        {
            logRequestedCPUs(execMetaData);
            operationContext.shutdown();
            logger.info(POD_TERMINATION_STRING);
        }
    }

    private void logRequestedCPUs(Map<String, Object> handlerMetada)
    {
        // todo If/when needed, make this logic more generic/smarter (e.g. set of metadata fields to check)

        double utilityPodCPURequest = 0;
        if(handlerMetada.keySet().contains(HandlerMetadata.RequestedCPUs.name()) && handlerMetada.get(HandlerMetadata.RequestedCPUs.name()) instanceof String)
        {
            Double utilityCPURequest = NumberUtils.toDouble((String) handlerMetada.get(HandlerMetadata.RequestedCPUs.name()), -1);

            if(utilityCPURequest >= 0)
            {
                utilityPodCPURequest += utilityCPURequest;
            }
        }
        logger.info(String.format(CPU_Template, DEFAULT_HANDLER_CPU_REQUEST + utilityPodCPURequest));
    }

    private String getOperationName()
    {
        return handlerMetadataRetriever.getMetadata().get(HandlerField.OPERATION_NAME.name());
    }

    /*
        date cid=** operationId=** owner=** conclustionStatus=**  elapsedTime-milliseconds=** operation=for-example-ffmpeg.filmstrip.0 payload=json-blob
 */
    private void logFinalStateAndDuration(String conclusionStatus)
    {
        Boolean seesMetadata = launchDataWrapper.getEnvironmentRetriever() != null;

        // Set default values
        String operationId = "operation id not visible";
        String owner = "owner not visible";
        String operation = "operation name not visible";
        String payload = "payload not visible";

        if(seesMetadata)
        {
            operationId = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.OPERATION_ID.name(), operationId);
            owner = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.CUSTOMER_ID.name(), owner);
            operation = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.OPERATION_NAME.name(), operation);
            payload = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.PAYLOAD.name(),payload);
        }
        String cid = setupLoggingCid();
        Long elapsedtime = System.currentTimeMillis() - start;
        logger.info(String.format(OPERATION_METRICS_TEMPLATE, cid, operationId, owner, conclusionStatus, elapsedtime.toString(), operation, payload));
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
    protected String setupLoggingCid()
    {
        if(MDC.get(HandlerField.CID.name())!= null)
        {
            return MDC.get(HandlerField.CID.name());
        }
        FieldRetriever fieldRetriever = launchDataWrapper.getEnvironmentRetriever();
        String cid = fieldRetriever != null
                     ? fieldRetriever.getField(HandlerField.CID.name(), UUID.randomUUID().toString())
                     : UUID.randomUUID().toString();
        MDC.put(HandlerField.CID.name(), cid);
        return cid;
    }
}
