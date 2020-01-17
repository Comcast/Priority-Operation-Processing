package com.theplatform.dfh.cp.handler.sample.impl.processor;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.base.processor.BaseJsonOperationProcessor;
import com.theplatform.dfh.cp.handler.base.progress.reporter.operation.OperationProgressReporter;
import com.theplatform.dfh.cp.handler.sample.api.ActionParameters;
import com.theplatform.dfh.cp.handler.sample.api.SampleAction;
import com.theplatform.dfh.cp.handler.sample.api.SampleInput;
import com.theplatform.dfh.cp.handler.sample.impl.action.ActionMap;
import com.theplatform.dfh.cp.handler.sample.impl.action.BaseAction;
import com.theplatform.dfh.cp.handler.sample.impl.context.OperationContext;
import com.theplatform.dfh.cp.handler.sample.impl.exception.DfhSampleException;
import com.theplatform.dfh.cp.handler.sample.impl.progress.SampleJobInfo;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Basic processor for running the sample action and requesting the output is parsed
 */
public class SampleActionProcessor extends BaseJsonOperationProcessor<SampleInput, LaunchDataWrapper, OperationContext>
{
    private static Logger logger = LoggerFactory.getLogger(SampleActionProcessor.class);

    private JsonHelper jsonHelper;
    private ActionMap actionMap;

    public SampleActionProcessor(OperationContext operationContext)
    {
        super(operationContext);
        this.jsonHelper = new JsonHelper();
        this.actionMap = new ActionMap();
    }

    @Override
    protected void execute(SampleInput sampleInput)
    {
        // load prior progress
        SampleJobInfo sampleJobInfo = loadPriorProgress();
        if(sampleJobInfo != null)
        {
            logger.info("Loaded prior progress: {}", jsonHelper.getJSONString(sampleJobInfo));
        }
        else
        {
            logger.info("No prior progress was loaded.");
        }

        try
        {
            OperationProgressReporter reporter = operationContext.getOperationProgressReporter();
            List<SampleAction> sampleActionList = sampleInput.getActions();
            // TODO: foreach exception handling check / test
            if(sampleActionList != null) sampleActionList.forEach(action -> performAction(action, reporter));

            // the result is always the payload indicated on the input
            reporter.addSucceeded(sampleInput.getResultPayload());
            // TODO: consider pushing this into the base context
        }
        catch(Exception e)
        {
            // TODO: handlers should exit gracefully...
            throw new RuntimeException("Failed to load/execute payload.", e);
        }
    }

    protected SampleJobInfo loadPriorProgress()
    {
        OperationProgress lastProgress = operationContext.getLaunchDataWrapper().getLastOperationProgress();
        if(lastProgress == null)
        {
            return null;
        }
        Object rawJobInfo = lastProgress.getParams().get(SampleJobInfo.PARAM_NAME);
        if(rawJobInfo == null)
        {
            return null;
        }
        return jsonHelper.getObjectFromMap(jsonHelper.getMapFromObject(rawJobInfo), SampleJobInfo.class);
    }

    @Override
    public Class<SampleInput> getPayloadClassType()
    {
        return SampleInput.class;
    }

    public void performAction(SampleAction sampleAction, OperationProgressReporter reporter)
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
