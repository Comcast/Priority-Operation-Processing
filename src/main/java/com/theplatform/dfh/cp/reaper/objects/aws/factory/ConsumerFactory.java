package com.theplatform.dfh.cp.reaper.objects.aws.factory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.dfh.cp.reaper.objects.aws.config.DataObjectReaperConfig;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedDeleter;

public class ConsumerFactory
{
    public Consumer<String> createBatchedDeleter(AmazonDynamoDB dynamoDB, DataObjectReaperConfig reaperConfig)
    {
        return new BatchedDeleter(
            dynamoDB,
            reaperConfig.getTableName(),
            reaperConfig.getIdFieldName())
            .setDeleteCallDelayMillis(reaperConfig.getDeleteCallDelayMillis());
    }
}
