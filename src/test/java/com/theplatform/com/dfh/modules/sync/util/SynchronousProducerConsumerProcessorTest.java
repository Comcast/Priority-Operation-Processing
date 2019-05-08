package com.theplatform.com.dfh.modules.sync.util;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Note: These all have warnings because the types do not have a generic specified (not necessary)
 */
public class SynchronousProducerConsumerProcessorTest
{
    private Consumer mockConsumer;
    private Producer mockProducer;
    private SynchronousProducerConsumerProcessor processor;

    @BeforeMethod
    public void setup()
    {
        mockProducer = mock(Producer.class);
        mockConsumer = mock(Consumer.class);
        processor = new SynchronousProducerConsumerProcessor(mockProducer, mockConsumer);
    }

    @Test
    public void testImmediateTimeout()
    {
        processor.setRunMaxSeconds(0);
        // the producer needs to return something
        doReturn(createProducerResult()).when(mockProducer).produce(any());
        SynchronousProducerConsumerProcessor spyProcessor = spy(processor);
        spyProcessor.execute();
        verify(mockConsumer, times(0)).consume(any(), any());
        verify(spyProcessor, times(1)).processingTimeExpired();
    }

    @DataProvider
    public Object[][] noDataProvider()
    {
        return new Object[][]
            {
                {null},
                {new ArrayList<>()}
            };
    }

    @Test(dataProvider = "noDataProvider")
    public void testNoDataProduced(Collection collection)
    {
        doReturn(new ProducerResult().setItemsProduced(collection)).when(mockProducer).produce(any());
        processor.execute();
        verify(mockConsumer, times(0)).consume(any(), any());
    }

    @Test
    public void testConsumerInterrupt()
    {
        ConsumerResult consumerResult = new ConsumerResult().setInterrupted(true);
        doReturn(consumerResult).when(mockConsumer).consume(any(), any());
        // the producer needs to return something
        doReturn(createProducerResult()).when(mockProducer).produce(any());
        SynchronousProducerConsumerProcessor spyProcessor = spy(processor);
        spyProcessor.execute();
        verify(mockConsumer, times(1)).consume(any(), any());
        verify(spyProcessor, times(0)).processingTimeExpired();
    }

    @Test
    public void testConsumer()
    {
        // the producer needs to return something the first time
        doReturn(new ConsumerResult<>()).when(mockConsumer).consume(any(), any());
        doAnswer(new Answer()
        {
            int producerCalls = 0;
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                producerCalls++;
                return producerCalls == 1
                       ? createProducerResult()
                       : new ProducerResult();

            }
        }).when(mockProducer).produce(any());
        SynchronousProducerConsumerProcessor spyProcessor = spy(processor);
        spyProcessor.execute();
        verify(mockConsumer, times(1)).consume(any(), any());
    }

    private ProducerResult createProducerResult()
    {
        ProducerResult producerResult = new ProducerResult();
        producerResult.setItemsProduced(Arrays.asList(1));
        return producerResult;
    }
}
