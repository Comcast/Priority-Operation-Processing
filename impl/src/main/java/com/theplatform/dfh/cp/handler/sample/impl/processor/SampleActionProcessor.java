package com.theplatform.dfh.cp.handler.sample.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.field.retriever.properties.PropertyProvider;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.sample.api.ActionParameters;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;
import com.theplatform.dfh.cp.handler.sample.impl.action.ActionMap;
import com.theplatform.dfh.cp.handler.sample.impl.action.BaseAction;
import com.theplatform.dfh.cp.handler.sample.impl.context.OperationContext;
import com.theplatform.dfh.cp.handler.sample.impl.exception.DfhSampleException;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic processor for running the sample action and requesting the output is parsed
 */
public class SampleActionProcessor implements HandlerProcessor<Void>
{
    private static Logger logger = LoggerFactory.getLogger(SampleActionProcessor.class);

    private LaunchDataWrapper launchDataWrapper;
    private OperationContext operationContext;
    private JsonHelper jsonHelper;
    private ActionMap actionMap;

    public SampleActionProcessor(LaunchDataWrapper launchDataWrapper, OperationContext operationContext)
    {
        this.launchDataWrapper = launchDataWrapper;
        this.operationContext = operationContext;
        this.jsonHelper = new JsonHelper();
        this.actionMap = new ActionMap();
    }

    /**
     * Executes the necessary steps to perform the action
     */
    public Void execute()
    {
        SampleInput handlerInput;
        ActionParameters actionParameters;
        Reporter reporter = operationContext.getReporter();

        try
        {
            logger.info("Reading payload");
            handlerInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), SampleInput.class);
            // convert the params map to a ActionParameters (Jackson can do this without converting to intermediate json)
            actionParameters = jsonHelper.getObjectFromMap(handlerInput.getParamsMap(), ActionParameters.class);

        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to load payload.", e);
        }

        reporter.reportProgress(handlerInput);

        BaseAction baseAction = actionMap.getAction(handlerInput.getAction());
        if(baseAction != null)
        {
            logger.info("Performing Action: {}", handlerInput.getAction());
            baseAction.performAction(reporter, actionParameters);
        }
        else
        {
            throw new DfhSampleException(String.format("Invalid action specified: %1$s", handlerInput.getAction()));
        }

        reporter.reportSuccess("All done!");
        return null;
    }

    public void setLaunchDataWrapper(LaunchDataWrapper launchDataWrapper)
    {
        this.launchDataWrapper = launchDataWrapper;
    }

    public void setOperationContext(OperationContext operationContext)
    {
        this.operationContext = operationContext;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setActionMap(ActionMap actionMap)
    {
        this.actionMap = actionMap;
    }
}
