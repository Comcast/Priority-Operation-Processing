package com.comcast.fission.reaper.objects.aws.factory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.comcast.fission.reaper.objects.aws.config.DataObjectReaperConfig;
import com.comcast.fission.reaper.objects.aws.dynamo.BatchedReapCandidatesRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

public class ProducerFactory
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public Producer<String> createBatchedReapCandidatesRetriever(AmazonDynamoDB dynamoDB, DataObjectReaperConfig reaperConfig)
    {
        return new BatchedReapCandidatesRetriever(
            dynamoDB,
            reaperConfig.getTableName(),
            reaperConfig.getIdFieldName(),
            reaperConfig.getTimeFieldName(),
            Instant.now().minusSeconds(reaperConfig.getReapAgeMinutes() * 60).toEpochMilli())
            .setTargetBatchSize(reaperConfig.getTargetBatchSize())
            .setObjectScanLimit(reaperConfig.getObjectScanLimit())
            .setScanDelayMillis(reaperConfig.getScanDelayMillis());
    }
}
