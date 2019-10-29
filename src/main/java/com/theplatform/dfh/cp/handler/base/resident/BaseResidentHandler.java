package com.theplatform.dfh.cp.handler.base.resident;

import com.theplatform.dfh.cp.api.progress.CompleteStateMessage;
import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.base.progress.OperationProgressFactory;
import com.theplatform.dfh.cp.handler.base.translator.JsonPayloadTranslator;
import com.theplatform.dfh.cp.handler.base.translator.PayloadTranslationResult;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.api.FieldRetriever;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class BaseResidentHandler<T, O extends OperationProgressFactory> implements ResidentHandler
{
    private LaunchDataWrapper launchDataWrapper;
    private ProgressReporter progressReporter;
    private JsonHelper jsonHelper;

    public BaseResidentHandler()
    {
        this.jsonHelper = new JsonHelper();
    }

    @Override
    public String execute(String payload, LaunchDataWrapper launchDataWrapper, ProgressReporter<OperationProgress> reporter)
    {
        this.progressReporter = reporter;
        this.launchDataWrapper = launchDataWrapper;
        PayloadTranslationResult<T> translationResult = new JsonPayloadTranslator<T>(jsonHelper)
            .translatePayload(payload, getPayloadClassType());
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

    public ProgressReporter getProgressReporter()
    {
        return progressReporter;
    }

    public void setProgressReporter(ProgressReporter progressReporter)
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
}
