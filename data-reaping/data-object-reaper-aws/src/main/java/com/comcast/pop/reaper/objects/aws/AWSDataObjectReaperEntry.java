package com.comcast.pop.reaper.objects.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.comcast.pop.reaper.objects.aws.config.DataObjectReaperConfig;
import com.comcast.pop.reaper.objects.aws.factory.ConsumerFactory;
import com.comcast.pop.reaper.objects.aws.factory.ProducerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.pop.modules.sync.util.Consumer;
import com.comcast.pop.modules.sync.util.Producer;
import com.comcast.pop.modules.sync.util.SynchronousProducerConsumerProcessor;
import com.comcast.pop.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.comcast.pop.version.info.ServiceBuildPropertiesContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Main entry point class for a CloudWatch Event trigger
 *
 * The incoming request from an event is whatever is specified in the event (assuming constant JSON text)
 */
public class AWSDataObjectReaperEntry implements RequestStreamHandler
{
    private static ObjectMapper objectMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProducerFactory producerFactory = new ProducerFactory();
    private ConsumerFactory consumerFactory = new ConsumerFactory();
    private AWSDynamoDBFactory awsDynamoDBFactory = new AWSDynamoDBFactory();

    public AWSDataObjectReaperEntry()
    {
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger, false);

        DataObjectReaperConfig reaperConfig;
        try
        {
            reaperConfig = objectMapper.readValue(inputStream, DataObjectReaperConfig.class);
        }
        catch(IOException e)
        {
            throw new RuntimeException("Failed to read input as DataObjectReaperConfig.", e);
        }
        if(reaperConfig == null)
        {
            throw new RuntimeException("Reaper cannot operate with a null DataObjectReaperConfig.");
        }

        String validationResult = reaperConfig.validate();
        if(validationResult != null)
            throw new RuntimeException(String.format("Configuration validation failed: %1$s", validationResult));

        AmazonDynamoDB dynamoDB = awsDynamoDBFactory.getAmazonDynamoDB();

        Producer<String> producer = producerFactory.createBatchedReapCandidatesRetriever(dynamoDB, reaperConfig);
        Consumer<String> consumer = consumerFactory.createBatchedDeleter(dynamoDB, reaperConfig);

        logger.info(producer.toString());
        logger.info(consumer.toString());

        SynchronousProducerConsumerProcessor<String> processor =
            new SynchronousProducerConsumerProcessor<>(producer, consumer)
                .setRunMaxSeconds(reaperConfig.getMaximumExecutionSeconds());

        processor.execute();
    }
}
