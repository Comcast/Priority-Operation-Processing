package com.comcast.pop.reaper.objects.aws.factory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.comcast.pop.modules.sync.util.Consumer;
import com.comcast.pop.reaper.objects.aws.config.DataObjectReaperConfig;
import com.comcast.pop.reaper.objects.aws.dynamo.BatchedDeleter;

public class ConsumerFactory
{
    public Consumer<String> createBatchedDeleter(AmazonDynamoDB dynamoDB, DataObjectReaperConfig reaperConfig)
    {
        return new BatchedDeleter(
            dynamoDB,
            reaperConfig.getTableName(),
            reaperConfig.getIdFieldName())
            .setDeleteCallDelayMillis(reaperConfig.getDeleteCallDelayMillis())
            .setLogDeleteOnly(reaperConfig.isLogDeleteOnly());
    }
}
