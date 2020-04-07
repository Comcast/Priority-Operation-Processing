package com.comast.pop.handler.base.context;

import com.comast.pop.handler.base.config.IntermediatePathComposer;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.comast.pop.handler.base.progress.OperationProgressFactory;
import com.comast.pop.handler.base.progress.reporter.operation.OperationProgressReporter;
import com.comast.pop.handler.base.progress.reporter.operation.OperationProgressReporterImpl;
import com.comast.pop.handler.base.progress.reporter.operation.OperationProgressThread;
import com.comast.pop.handler.base.progress.reporter.operation.OperationProgressThreadConfig;
import com.comast.pop.handler.base.reporter.ProgressReporter;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;

public abstract class ProgressOperationContext<T extends LaunchDataWrapper> extends BaseOperationContext<T>
{
    private OperationProgressReporter operationProgressReporter;
    private OperationProgressThread operationProgressThread;
    private IntermediatePathComposer intermediatePathComposer;

    /**
     * Constructor
     * @param reporter The underlying reporter to send updates via
     * @param launchDataWrapper The LaunchDataWrapper to associate with this context
     */
    public ProgressOperationContext(ProgressReporter<OperationProgress> reporter, T launchDataWrapper)
    {
        super(launchDataWrapper);
        operationProgressThread = new OperationProgressThread(
            new OperationProgressThreadConfig()
                .setReporter(reporter)
        );
        this.operationProgressReporter = new OperationProgressReporterImpl(operationProgressThread, new OperationProgressFactory());
        this.intermediatePathComposer = new IntermediatePathComposer();
    }

    /**
     * Constructor
     * @param operationProgressThread The OperationProgressThread to associate with the context and use for sending updates
     * @param operationProgressReporter The OperationProgressReporter to associate with the context
     * @param launchDataWrapper The LaunchDataWrapper to associate with this context
     */
    public ProgressOperationContext(OperationProgressThread operationProgressThread, OperationProgressReporter operationProgressReporter, T launchDataWrapper)
    {
        super(launchDataWrapper);
        this.operationProgressThread = operationProgressThread;
        this.operationProgressReporter = operationProgressReporter;
        this.intermediatePathComposer = new IntermediatePathComposer();
    }

    @Override
    public void init()
    {
        operationProgressThread.init();
    }

    @Override
    public void shutdown()
    {
        operationProgressThread.shutdown(false);
    }

    @Override
    public void processUnhandledException(String message, Exception e)
    {
        if(operationProgressReporter == null) return;
        operationProgressReporter.addFailed(null, new DiagnosticEvent(message, e));
    }

    public OperationProgressReporter getOperationProgressReporter()
    {
        return operationProgressReporter;
    }

    public void setOperationProgressReporter(OperationProgressReporter operationProgressReporter)
    {
        this.operationProgressReporter = operationProgressReporter;
    }

    public ProgressOperationContext<T> setIntermediatePathComposer(IntermediatePathComposer intermediatePathComposer)
    {
        this.intermediatePathComposer = intermediatePathComposer;
        return this;
    }

    public String getTemporaryPath(ParamsMap paramsMap)
    {
        return getTemporaryPath( paramsMap, null);
    }

    public String getTemporaryPath(ParamsMap paramsMap, String defaultIntermediatePath)
    {
        return intermediatePathComposer.retrieveIntermediatePath(getLaunchDataWrapper(), paramsMap, defaultIntermediatePath);
    }
}
