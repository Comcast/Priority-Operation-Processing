package com.comcast.pop.modules.queue.aws.sqs.processor;

import com.comcast.pop.modules.queue.aws.sqs.SQSRequestContext;
import com.comcast.pop.modules.queue.aws.sqs.api.QueueRequest;
import com.comcast.pop.modules.queue.aws.sqs.api.QueueRequestType;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TypeRequestProcessorTest
{
    private TypeRequestProcessor typeRequestProcessor;

    @BeforeMethod
    public void setup()
    {
        typeRequestProcessor = new TypeRequestProcessor();
    }

    @Test
    public void testExecuteSQSActionProcessorException()
    {
        final String REQUEST_TYPE = QueueRequestType.add.name();

        SQSRequestProcessor mockProcessor = mock(SQSRequestProcessor.class);
        doThrow(new RuntimeException()).when(mockProcessor).processRequest(any());
        Map<String, SQSRequestProcessor> requestProcessorMap = new HashMap<>();
        requestProcessorMap.put(REQUEST_TYPE, mockProcessor);
        typeRequestProcessor.setRequestProcessorMap(requestProcessorMap);

        Assert.assertFalse(typeRequestProcessor.processRequest(
                new SQSRequestContext(
                        null,
                        new QueueRequest(REQUEST_TYPE, null, null, null),
                        null)).isSuccessful());
        verify(mockProcessor, times(1)).processRequest(any());
    }

    @Test
    public void testExecuteSQSActionUnkownProcessor()
    {
        Assert.assertFalse(typeRequestProcessor.processRequest(
                new SQSRequestContext(
                        null,
                        new QueueRequest("unknown", null, null, null),
                        null))
                .isSuccessful());
    }

    @DataProvider
    public Object[][] mappedRequestTypes()
    {
        QueueRequestType[] allTypes = QueueRequestType.values();
        Object[][] types = new Object[allTypes.length][1];
        IntStream.range(0, allTypes.length).forEach(i -> types[i][0] = allTypes[i].name());
        return types;
    }

    @Test(dataProvider = "mappedRequestTypes")
    public void testDefaultRequestProcessors(String requestType)
    {
        Assert.assertNotNull(typeRequestProcessor.getRequestProcessorMap().get(requestType));
    }
}
