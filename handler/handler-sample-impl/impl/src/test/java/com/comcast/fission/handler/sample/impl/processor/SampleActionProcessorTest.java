package com.comcast.fission.handler.sample.impl.processor;

import com.theplatform.dfh.cp.handler.base.field.retriever.LaunchDataWrapper;
import com.comcast.fission.handler.sample.impl.context.OperationContext;
import com.comcast.fission.handler.sample.impl.progress.SampleJobInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class SampleActionProcessorTest
{
    private static final String JOB_ID = "123456-987654";

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
        doReturn(new SampleJobInfo(JOB_ID)).when(mockLaunchDataWrapper).getLastOperationProgressParam(anyString(), any(Class.class));
        SampleJobInfo sampleJobInfo = sampleActionProcessor.loadPriorProgress();
        Assert.assertEquals(sampleJobInfo.getJobId(), JOB_ID);
    }
}
