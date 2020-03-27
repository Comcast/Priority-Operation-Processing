package com.theplatform.dfh.cp.reaper.objects.aws;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.com.dfh.modules.sync.util.SynchronousProducerConsumerProcessor;
import com.theplatform.dfh.cp.reaper.objects.aws.config.DataObjectReaperConfig;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedDeleter;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedReapCandidatesRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

public class ManualTests
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //@Test
    public void executeOnTable() throws Exception
    {
        final String PROFILE_NAME = "lab_Fission";

        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(new ProfileCredentialsProvider(PROFILE_NAME))
            .withRegion(Regions.US_WEST_2)
            .build();

        final String TABLE = "Fission-Twinkle-ProgressOperation-dev";

        SynchronousProducerConsumerProcessor<String> processor = new SynchronousProducerConsumerProcessor<>(
            new BatchedReapCandidatesRetriever(amazonDynamoDB, TABLE, "id", "updatedTime", 1557367401477L)
                .setScanDelayMillis(500)
                .setObjectScanLimit(25)
                .setTargetBatchSize(75),
            new BatchedDeleter(amazonDynamoDB, TABLE, "id")
                .setDeleteCallDelayMillis(1000)
                .setLogDeleteOnly(true)
        )
        .setRunMaxSeconds(3600);

        processor.execute();

        ObjectMapper objectMapper = new ObjectMapper();

        logger.info(objectMapper.writeValueAsString(new DataObjectReaperConfig()));

    }
}
