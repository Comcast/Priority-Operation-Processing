package com.theplatform.dfh.cp.reaper.objects.aws;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.com.dfh.modules.sync.util.SynchronousProducerConsumerProcessor;
import com.theplatform.dfh.cp.reaper.objects.aws.config.DataObjectReaperConfig;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedDeleter;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedObjectFieldRetriever;
import com.theplatform.dfh.cp.reaper.objects.aws.factory.ConsumerFactory;
import com.theplatform.dfh.cp.reaper.objects.aws.factory.ProducerFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.version.info.ServiceBuildPropertiesContainer;
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
public class AWSLambdaStreamEntry implements RequestStreamHandler
{
    private static ObjectMapper objectMapper = new ObjectMapper();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ProducerFactory producerFactory = new ProducerFactory();
    private ConsumerFactory consumerFactory = new ConsumerFactory();
    private AWSDynamoDBFactory awsDynamoDBFactory = new AWSDynamoDBFactory();

    public AWSLambdaStreamEntry(
    )
    {
    }

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        ServiceBuildPropertiesContainer.logServiceBuildString(logger);

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

        AmazonDynamoDB dynamoDB = awsDynamoDBFactory.getAmazonDynamoDB();

        SynchronousProducerConsumerProcessor<String> processor = new SynchronousProducerConsumerProcessor<>(
            producerFactory.createBatchedReapCandidatesRetriever(dynamoDB, reaperConfig),
            consumerFactory.createBatchedDeleter(dynamoDB, reaperConfig)
        )
        .setRunMaxSeconds(reaperConfig.getMaximumExecutionSeconds());

        processor.execute();
    }
}
