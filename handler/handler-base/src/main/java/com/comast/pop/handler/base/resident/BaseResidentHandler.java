package com.comast.pop.handler.base.resident;

import com.comast.pop.handler.base.ResidentHandler;
import com.comast.pop.handler.base.ResidentHandlerParams;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.field.retriever.api.FieldRetriever;
import com.comast.pop.handler.base.progress.OperationProgressFactory;
import com.comast.pop.handler.base.reporter.ProgressReporter;
import com.comast.pop.handler.base.translator.JsonPayloadTranslator;
import com.comast.pop.handler.base.translator.PayloadTranslationResult;
import com.comcast.pop.api.progress.CompleteStateMessage;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.comcast.pop.modules.jsonhelper.JsonHelper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Convenience ResidentHandler base providing translation of the payload
 * @param <T> The type of input the handler expects
 * @param <O> The type of operation progress factory
 */
public abstract class BaseResidentHandler<T, O extends OperationProgressFactory> implements ResidentHandler
{
    private LaunchDataWrapper launchDataWrapper;
    private ProgressReporter progressReporter;
    private JsonHelper jsonHelper;
    private ResidentHandlerParams residentHandlerParams;

    public BaseResidentHandler()
    {
        this.jsonHelper = new JsonHelper();
    }

    @Override
    public String execute(ResidentHandlerParams residentHandlerParams)
    {
        this.progressReporter = residentHandlerParams.getReporter();
        this.residentHandlerParams = residentHandlerParams;
        this.launchDataWrapper = residentHandlerParams.getLaunchDataWrapper();
        PayloadTranslationResult<T> translationResult = new JsonPayloadTranslator<T>(jsonHelper)
            .translatePayload(residentHandlerParams.getPayload(), getPayloadClassType());
        if(!translationResult.isSuccessful())
        {
            processLoadFailure(Collections.singletonList(translationResult.getDiagnosticEvent()));
            return null;
        }
        if(shouldExecutionContinue(translationResult.getObject()))
            return execute(translationResult.getObject());
        return null;
    }

    public abstract String execute(T handlerInput);

    /**
     * Evaluates the input object and determines if execution should continue. Fails the operation if any issues are reported.
     * @param inputObject The input object to evaluate
     * @return true if there are no issues, false otherwise
     */
    protected boolean shouldExecutionContinue(T inputObject)
    {
        List<DiagnosticEvent> diagnosticEvents = validateInput(inputObject);
        if(diagnosticEvents == null || diagnosticEvents.size() == 0) return true;
        processLoadFailure(diagnosticEvents);
        return false;
    }

    /**
     * Method for performing validation on the input object before execution.
     * @param inputObject The input to verify.
     * @return List of DiagnosticEvents to report if there is an issue. null or empty list if there are no issues.
     */
    protected List<DiagnosticEvent> validateInput(T inputObject)
    {
        return null;
    }

    public LaunchDataWrapper getLaunchDataWrapper()
    {
        return launchDataWrapper;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public ProgressReporter<OperationProgress> getProgressReporter()
    {
        return progressReporter;
    }

    public void setProgressReporter(ProgressReporter<OperationProgress> progressReporter)
    {
        this.progressReporter = progressReporter;
    }

    public JsonHelper getJsonHelper()
    {
        return jsonHelper;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    /**
     * Processes the failure to translate the payload into the target object
     * @param diagnosticEvents The events to report
     */
    protected void processLoadFailure(List<DiagnosticEvent> diagnosticEvents)
    {
        progressReporter.reportProgress(
            getOperationProgressFactory().createWithEvents(ProcessingState.COMPLETE, CompleteStateMessage.FAILED.toString(), diagnosticEvents));
    }

    public abstract O getOperationProgressFactory();

    public abstract Class<T> getPayloadClassType();

    public String getCID()
    {
        FieldRetriever environmentRetriever = launchDataWrapper.getEnvironmentRetriever();
        if(environmentRetriever == null)
            return UUID.randomUUID().toString();
        return environmentRetriever.getField(HandlerField.CID.name(), UUID.randomUUID().toString());
    }

    public ResidentHandlerParams getResidentHandlerParams()
    {
        return residentHandlerParams;
    }

    public void setResidentHandlerParams(ResidentHandlerParams residentHandlerParams)
    {
        this.residentHandlerParams = residentHandlerParams;
    }
}
