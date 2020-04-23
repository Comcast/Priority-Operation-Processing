package com.comcast.pop.handler.sample.impl.processor;

import com.comcast.pop.handler.sample.impl.action.ActionMap;
import com.comcast.pop.handler.sample.impl.action.ExternalExecuteAction;
import com.comcast.pop.handler.sample.impl.context.OperationContext;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.processor.BaseJsonOperationProcessor;
import com.comast.pop.handler.base.progress.reporter.operation.OperationProgressReporter;
import com.comcast.pop.handler.kubernetes.support.config.KubeConfigFactoryImpl;
import com.comcast.pop.handler.kubernetes.support.config.NfsDetailsFactoryImpl;
import com.comcast.pop.handler.sample.api.ActionParameters;
import com.comcast.pop.handler.sample.api.SampleAction;
import com.comcast.pop.handler.sample.api.SampleActions;
import com.comcast.pop.handler.sample.api.SampleInput;
import com.comcast.pop.handler.sample.impl.action.BaseAction;
import com.comcast.pop.handler.sample.impl.exception.SampleHandlerException;
import com.comcast.pop.handler.sample.impl.executor.kubernetes.KubernetesExternalExecutorFactory;
import com.comcast.pop.handler.sample.impl.progress.SampleJobInfo;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
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
        appendAdditionalActions();

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

    protected void appendAdditionalActions()
    {
        // TODO: add others, detect if local vs. not (etc!)
        actionMap.addAction(SampleActions.externalExecute.name(),
            new ExternalExecuteAction(new KubernetesExternalExecutorFactory(
                new KubeConfigFactoryImpl(launchDataWrapper),
                new NfsDetailsFactoryImpl(launchDataWrapper)),
                launchDataWrapper));
    }

    protected SampleJobInfo loadPriorProgress()
    {
        return operationContext.getLaunchDataWrapper().getLastOperationProgressParam(SampleJobInfo.PARAM_NAME, SampleJobInfo.class);
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
            throw new SampleHandlerException(String.format("Invalid action specified: %1$s", sampleAction.getAction()));
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
