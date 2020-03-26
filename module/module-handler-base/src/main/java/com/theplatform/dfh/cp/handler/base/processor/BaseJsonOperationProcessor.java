package com.theplatform.dfh.cp.handler.base.processor;

import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.handler.base.context.ProgressOperationContext;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.translator.JsonPayloadTranslator;
import com.theplatform.dfh.cp.handler.base.translator.PayloadTranslationResult;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Convenience Processor for operations that performs the basic translation from Json to a type for execution
 * @param <T> The type of object to translate to
 * @param <L> The LaunchDataWrapper type
 * @param <C> The ProgressOperationContext type
 */
public abstract class BaseJsonOperationProcessor<T, L extends LaunchDataWrapper, C extends ProgressOperationContext<L>> extends AbstractBaseHandlerProcessor<L, C>
{
    private JsonHelper jsonHelper = new JsonHelper();

    public BaseJsonOperationProcessor(C operationContext)
    {
        super(operationContext);
    }

    @Override
    public void execute()
    {
        PayloadTranslationResult<T> translationResult = new JsonPayloadTranslator<T>(jsonHelper)
            .translatePayload(launchDataWrapper.getPayload(), getPayloadClassType());
        if(!translationResult.isSuccessful())
        {
            processLoadFailure(Collections.singletonList(translationResult.getDiagnosticEvent()));
            return;
        }
        if(shouldExecutionContinue(translationResult.getObject()))
            execute(translationResult.getObject());
    }

    /**
     * Evaluates the input object and determines if execution should continue. Fails the operation if any issues are reported.
     * @param inputObject The input object to evaluate
     * @return true if there are no issues, false otherwise
     */
    protected boolean shouldExecutionContinue(T inputObject)
    {
        List<DiagnosticEvent> diagnosticEvents = validateInput(inputObject);
        if(diagnosticEvents == null || diagnosticEvents.size() == 0) return true;
        getOperationContext().getOperationProgressReporter().addFailed(diagnosticEvents, null);
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

    /**
     * Retrieves the last indicated number of entries from the end of the input list
     * @param entries The entries to extract from
     * @param entryLimit The number of entries to limit to
     * @return A collection with up to the indicated limit. If the input is null, empty, or the limit is less than 1 a new list is returned.
     */
    public static List<String> retrieveLastEntries(final List<String> entries, final int entryLimit)
    {
        if(entries == null || entries.size() == 0 || entryLimit < 1) return new ArrayList<>();
        int startIdx = 0;
        int endIdx = entries.size();
        if(endIdx > entryLimit)
            startIdx = endIdx - entryLimit;
        return IntStream.range(startIdx, endIdx)
            .mapToObj(entries::get)
            .collect(Collectors.toList());
    }

    /**
     * Processes the failure to translate the payload into the target object
     * @param diagnosticEvents The events to report
     */
    protected void processLoadFailure(List<DiagnosticEvent> diagnosticEvents)
    {
        operationContext.getOperationProgressReporter().addFailed(diagnosticEvents);
    }

    /**
     * Executes the instructions indicated in the payload object
     * @param payloadObject The translated object
     */
    protected abstract void execute(T payloadObject);

    /**
     * The class type of the payload
     * @return The type of the object
     */
    public abstract Class<T> getPayloadClassType();
}
