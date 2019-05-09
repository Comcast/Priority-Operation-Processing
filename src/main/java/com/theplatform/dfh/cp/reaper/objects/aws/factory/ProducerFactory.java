package com.theplatform.dfh.cp.reaper.objects.aws.factory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.dfh.cp.reaper.objects.aws.config.DataObjectReaperConfig;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedReapCandidatesRetriever;

import java.time.Instant;

public class ProducerFactory
{
    public Producer<String> createBatchedReapCandidatesRetriever(AmazonDynamoDB dynamoDB, DataObjectReaperConfig reaperConfig)
    {
        return new BatchedReapCandidatesRetriever(
            dynamoDB,
            reaperConfig.getTableName(),
            reaperConfig.getIdFieldName(),
            reaperConfig.getTimeFieldName(),
            Instant.now().minusSeconds(reaperConfig.getReapAgeMinutes() * 60).getEpochSecond())
            .setBatchSize(reaperConfig.getBatchSize())
            .setObjectScanLimit(reaperConfig.getObjectScanLimit())
            .setScanDelayMillis(reaperConfig.getScanDelayMillis());
    }
}
