package com.theplatform.dfh.cp.reaper.objects.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.theplatform.com.dfh.modules.sync.util.ProducerResult;
import com.theplatform.com.dfh.modules.sync.util.SynchronousProducerConsumerProcessor;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedDeleter;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedObjectFieldRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.time.Instant;

public class ManualTests
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /*
    @Test
    public void clearTable()
    {
        final String PROFILE_NAME = "saml";

        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider(PROFILE_NAME))
            .withRegion(Regions.US_WEST_2)
            .build();

        final String TABLE = "DFH-Fission-Rage-TransformRequest-SEA1";

        SynchronousProducerConsumerProcessor<String> processor = new SynchronousProducerConsumerProcessor<>(
            new BatchedObjectFieldRetriever(amazonDynamoDB, TABLE, "id")
                .setScanDelayMillis(1000)
                .setObjectScanLimit(75)
                .setBatchSize(75),
            new BatchedDeleter(amazonDynamoDB, TABLE, "id")
                .setDeleteCallDelayMillis(1000)
        );

        processor.setRunMaxSeconds(3600);
        processor.execute();

        logger.info("");

    }*/
}
