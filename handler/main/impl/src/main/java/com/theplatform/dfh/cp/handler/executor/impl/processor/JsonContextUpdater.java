package com.theplatform.dfh.cp.handler.executor.impl.processor;

import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.context.ExecutorContext;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * JsonContext specific onOperationComplete listener. Performs an update to the JsonContext using the Operation output payload.
 *
 */
public class JsonContextUpdater implements OnOperationCompleteListener, JsonContextReferenceParser
{
    private static Logger logger = LoggerFactory.getLogger(JsonContextUpdater.class);

    public static final String OUTPUT_SUFFIX = ".out";
    private ExecutorContext executorContext;

    public JsonContextUpdater(ExecutorContext executorContext)
    {
        this.executorContext = executorContext;
    }

    /**
     * On completion updates the json context with the output from the operation
     * @param operationWrapper The completed operation
     */
    @Override
    public void onComplete(OperationWrapper operationWrapper)
    {
        // TODO: if failed this operation should not add data... can just exit
        Operation operation = operationWrapper.getOperation();
        String outputPayload = operationWrapper.getOutputPayload();

        String contextKey = operation.getName() + OUTPUT_SUFFIX;
        logger.info("Persisting ContextKey: [{}] OperationId: [{}] with OUTPUT Payload: {}", contextKey, operation.getId(), outputPayload);
        executorContext.getJsonContext().addData(contextKey, outputPayload);
    }

    /**
     * Gets the operation names from the references specified.
     * @param references The references to re-map to operation names
     * @return Set of operation names.
     */
    public Set<String> getOperationNames(Set<String> references)
    {
        return references.stream().map(reference ->
            getReferenceName(executorContext.getJsonContext().getJsonReferenceReplacer(), reference)
        ).collect(Collectors.toSet());
    }

    /**
     * Gets the reference name from a string (assuming it is a valid reference)
     * @param jsonReferenceReplacer The replacer
     * @param reference
     * @return
     */
    protected static String getReferenceName(JsonReferenceReplacer jsonReferenceReplacer, String reference)
    {
        // extract the name only (removes the reference cruft and any path on it)
        String referenceName = jsonReferenceReplacer.getReferenceName(reference);
        if(referenceName != null)
        {
            // TODO: if we end up with other suffix values this should be broken out...
            // OUTPUT_SUFFIX is generally appended to the reference. Remove it so it is just the operation name.
            return referenceName.endsWith(OUTPUT_SUFFIX)
                   ? referenceName.substring(0, referenceName.length() - OUTPUT_SUFFIX.length())
                   : referenceName;
        }
        else
        {
            throw new RuntimeException(String.format("Invalid reference name found: %1$s", reference));
        }
    }
}
