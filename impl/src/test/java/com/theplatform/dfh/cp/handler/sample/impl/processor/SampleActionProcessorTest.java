package com.theplatform.dfh.cp.handler.sample.impl.processor;

import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.handler.sample.impl.context.OperationContext;
import com.theplatform.dfh.cp.handler.sample.impl.progress.SampleJobInfo;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SampleActionProcessorTest
{
    private static final String JOB_ID = "123456-987654";

    private JsonHelper jsonHelper = new JsonHelper();

    private SampleActionProcessor sampleActionProcessor;
    private OperationContext mockOperationContext;
    private LaunchDataWrapper mockLaunchDataWrapper;

    @BeforeMethod
    public void setup()
    {
        mockLaunchDataWrapper = mock(LaunchDataWrapper.class);
        mockOperationContext = mock(OperationContext.class);
        doReturn(mockLaunchDataWrapper).when(mockOperationContext).getLaunchDataWrapper();
        sampleActionProcessor = new SampleActionProcessor(mockOperationContext);
    }

    @Test
    public void testLoadPriorProgress()
    {
        doReturn(createOperationProgress(JOB_ID, true)).when(mockLaunchDataWrapper).getLastOperationProgress();
        SampleJobInfo sampleJobInfo = sampleActionProcessor.loadPriorProgress();
        Assert.assertEquals(sampleJobInfo.getJobId(), JOB_ID);
    }

    private OperationProgress createOperationProgress(String lastProgress, boolean createParams)
    {
        OperationProgress operationProgress = new OperationProgress();
        if(createParams)
        {
            ParamsMap paramsMap = new ParamsMap();
            if (lastProgress != null)
            {
                paramsMap.put(SampleJobInfo.PARAM_NAME, new SampleJobInfo(lastProgress));
            }
            operationProgress.setParams(paramsMap);
        }
        return operationProgress;
    }
}
