package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.progress.JobProgress;
import com.theplatform.dfh.cp.api.progress.JobStatus;
import com.theplatform.dfh.cp.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.executor.api.ExecutorHandlerInput;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.handler.executor.impl.executor.kubernetes.KubernetesOperationExecutor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.util.http.impl.exception.HttpRequestHandlerException;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.ReferenceReplacementResult;
import com.theplatform.dfh.schedule.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.schedule.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.module.authentication.client.EncryptedAuthenticationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic test/local/prototype processor for running the Agenda
 */
public class SequentialAgendaProcessor implements HandlerProcessor<Void>
{
    private static Logger logger = LoggerFactory.getLogger(SequentialAgendaProcessor.class);

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

        if(handlerInput == null)
        {
            executorContext.getReporter().reportFailure("Invalid input. No payload.", null);
            return null;
        }

        if(handlerInput.getOperations() == null)
        {
            executorContext.getReporter().reportFailure("No operations in Agenda. Nothing to do.", null);
            return null;
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

        updateJobProgressStatus(handlerInput);
        logger.info("ExecutorComplete");
        return null;
    }

    protected void executeOperation(Operation operation)
    {
        try
        {
            // TODO: Use the information about missing/invalid references.
            ReferenceReplacementResult result = executorContext.getJsonContext().processReferences(operation.getPayload());

            BaseOperationExecutor executor = executorContext.getOperationExecutorFactory().generateOperationExecutor(executorContext, operation);
            String contextKey = operation.getName() + OUTPUT_SUFFIX;
            String outputPayload = executor.execute(result.getResult());
            logger.info("Persisting ContextKey: [{}] OperationId: [{}] with OUTPUT Payload: {}", contextKey, operation.getId(), outputPayload);
            executorContext.getJsonContext().addData(contextKey, outputPayload);
        }
        catch(Throwable t)
        {
            throw new AgendaExecutorException(
                String.format("Failed to execute operation: %1$s", operation == null ? "unknown!" : operation.getName())
                , t);
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


    /**
     * All belongs elsewhere...
     */
    public static final String IDM_URL_FIELD = "agenda.poster.idm.url";
    public static final String IDM_USER = "agenda.poster.idm.user";
    public static final String IDM_ENCRYPTED_PASS = "agenda.poster.idm.encryptedpass";

    private void updateJobProgressStatus(Agenda agenda)
    {
        try
        {
            String identityUrl = launchDataWrapper.getPropertyRetriever().getField(IDM_URL_FIELD);
            String user = launchDataWrapper.getPropertyRetriever().getField(IDM_USER);
            String encryptedPass = launchDataWrapper.getPropertyRetriever().getField(IDM_ENCRYPTED_PASS);
            if(identityUrl == null || user == null || encryptedPass == null)
            {
                throw new HttpRequestHandlerException("Invalid IDM credentials configured for token generation.");
            }
            HttpURLConnectionFactory httpURLConnectionFactory = new IDMHTTPUrlConnectionFactory(new EncryptedAuthenticationClient(
                identityUrl,
                user,
                encryptedPass,
                null
            ));

            HttpCPObjectClient<JobProgress> jobProgressClient = new HttpCPObjectClient<>(
                launchDataWrapper.getPropertyRetriever().getField("job.progress.url"),
                httpURLConnectionFactory,
                JobProgress.class
                );
            String progressId = agenda.getParams() == null ? null : agenda.getParams().getString(GeneralParamKey.progressId);
            JobProgress progress = jobProgressClient.getObject(progressId);
            // This is a hack
            if(progress.getStatus() == null || progress.getStatus() == JobStatus.INITIALIZE_EXECUTING)
            {
                progress.setStatus(JobStatus.INITIALIZE_COMPLETE);
            }
            else
            {
                progress.setStatus(JobStatus.RUN_COMPLETE);
            }
            jobProgressClient.updateObject(progress);
        }
        catch(Exception e)
        {
            logger.error("Failed to update status on progress object.", e);
        }
    }
}
