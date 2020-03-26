package com.theplatform.com.dfh.modules.sync.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Synchronously process a producer and consumer (produce then consume until complete)
 *
 * This is not a thread safe class.
 * @param <T> Type of objects to produce and consume
 */
public class SynchronousProducerConsumerProcessor<T>
{
    private static Logger logger = LoggerFactory.getLogger(SynchronousProducerConsumerProcessor.class);
    public static final int DEFAULT_RUN_MAX_SECONDS = 60;

    private final Producer<T> producer;
    private final Consumer<T> consumer;

    private int runMaxSeconds = DEFAULT_RUN_MAX_SECONDS;
    private int itemsProcessedCount = 0;

    public SynchronousProducerConsumerProcessor(Producer<T> producer, Consumer<T> consumer)
    {
        this.producer = producer;
        this.consumer = consumer;
    }

    /**
     * Executes the Producer Consumer processing until no more items are produced or
     * when the runMaxSeconds time is hit
     */
    public void execute()
    {
        final Instant endProcessingTime = Instant.now().plusSeconds(runMaxSeconds);

        itemsProcessedCount = 0;
        producer.reset();
        while(true)
        {
            ProducerResult<T> producerResult = producer.produce(endProcessingTime);
            if(producerResult.isInterrupted()
                || producerResult.getItemsProduced() == null
                || producerResult.getItemsProduced().size() == 0
                || InstantUtil.isNowAfterOrEqual(endProcessingTime))
            {
                break;
            }

            ConsumerResult<T> consumerResult = consumer.consume(producerResult.getItemsProduced(), endProcessingTime);

            itemsProcessedCount += consumerResult.getItemsConsumedCount();

            if(consumerResult.isInterrupted())
            {
                break;
            }
        }

        if(InstantUtil.isNowAfterOrEqual(endProcessingTime))
        {
            processingTimeExpired();
        }
    }



    /**
     * Called when the processing time expires during execution
     */
    protected void processingTimeExpired()
    {

    }

    public int getItemsProcessedCount()
    {
        return itemsProcessedCount;
    }

    public int getRunMaxSeconds()
    {
        return runMaxSeconds;
    }

    public SynchronousProducerConsumerProcessor<T> setRunMaxSeconds(int runMaxSeconds)
    {
        this.runMaxSeconds = runMaxSeconds;
        return this;
    }
}
