package com.theplatform.dfh.cp.handler.kubernetes.support.config;

import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.retriever.argument.ArgumentRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.handler.base.field.retriever.properties.PropertyRetriever;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReader;
import com.theplatform.dfh.cp.handler.base.payload.PayloadReaderFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class KubernetesLaunchDataWrapperTest
{
    private KubernetesLaunchDataWrapper launchDataWrapper;
    private ArgumentRetriever mockArgumentRetriever;
    private PropertyRetriever mockPropertyRetriever;
    private EnvironmentFieldRetriever mockEnvironmentFieldRetriever;
    private PayloadReaderFactory mockPayloadReaderFactory;
    private PayloadReader mockPayloadReader;

    @BeforeMethod
    public void setup()
    {
        mockPayloadReader = mock(PayloadReader.class);
        mockPayloadReaderFactory = mock(PayloadReaderFactory.class);
        doReturn(mockPayloadReader).when(mockPayloadReaderFactory).createReader();

        mockArgumentRetriever = mock(ArgumentRetriever.class);
        mockPropertyRetriever = mock(PropertyRetriever.class);
        mockEnvironmentFieldRetriever = mock(EnvironmentFieldRetriever.class);

        launchDataWrapper = new KubernetesLaunchDataWrapper(mockArgumentRetriever, mockEnvironmentFieldRetriever, mockPropertyRetriever);
        launchDataWrapper.setPayloadReaderFactory(mockPayloadReaderFactory);
    }

    @Test
    public void testPayloadCache()
    {
        final String PAYLOAD = "{}";
        doReturn(PAYLOAD).when(mockPayloadReader).readPayload();
        Assert.assertEquals(launchDataWrapper.getPayload(), PAYLOAD);
        verify(mockPayloadReader, times(1)).readPayload();
        Assert.assertEquals(launchDataWrapper.getPayload(), PAYLOAD);
        verify(mockPayloadReader, times(1)).readPayload();
        launchDataWrapper.resetCachedPayload();
        Assert.assertEquals(launchDataWrapper.getPayload(), PAYLOAD);
        verify(mockPayloadReader, times(2)).readPayload();
    }
}
