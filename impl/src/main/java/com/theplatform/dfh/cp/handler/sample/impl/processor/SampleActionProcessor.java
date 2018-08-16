package com.theplatform.dfh.cp.handler.sample.impl.processor;

import com.theplatform.dfh.cp.handler.base.processor.HandlerProcessor;
import com.theplatform.dfh.cp.handler.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.reporter.api.Reporter;
import com.theplatform.dfh.cp.handler.sample.api.ActionParameters;
import com.theplatform.dfh.cp.handler.sample.api.SampleAction;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;
import com.theplatform.dfh.cp.handler.sample.impl.action.ActionMap;
import com.theplatform.dfh.cp.handler.sample.impl.action.BaseAction;
import com.theplatform.dfh.cp.handler.sample.impl.context.OperationContext;
import com.theplatform.dfh.cp.handler.sample.impl.exception.DfhSampleException;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

        Reporter reporter = operationContext.getReporter();

        try
        {
            handlerInput = jsonHelper.getObjectFromString(launchDataWrapper.getPayload(), SampleInput.class);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to load payload.", e);
        }

        reporter.reportProgress(handlerInput);

        List<SampleAction> sampleActionList = handlerInput.getActions();
        // TODO: foreach exception handling check / test
        if(sampleActionList != null) sampleActionList.forEach(action -> performAction(action, reporter));

        // the result is always the payload indicated on the input
        reporter.reportSuccess(handlerInput.getResultPayload());
        return null;
    }

    public void performAction(SampleAction sampleAction, Reporter reporter)
    {
        BaseAction baseAction = actionMap.getAction(sampleAction.getAction());
        // convert the params map to a ActionParameters (Jackson can do this without converting to intermediate json)
        ActionParameters actionParameters = jsonHelper.getObjectFromMap(sampleAction.getParamsMap(), ActionParameters.class);
        if(baseAction != null)
        {
            logger.info("Performing Action: {}", sampleAction.getAction());
            baseAction.performAction(reporter, actionParameters);
        }
        else
        {
            throw new DfhSampleException(String.format("Invalid action specified: %1$s", sampleAction.getAction()));
        }
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
