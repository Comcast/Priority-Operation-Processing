package com.cts.fission.scheduling.queue.aws.persistence;

import com.cts.fission.scheduling.queue.InsightScheduleInfo;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentInsightScheduleInfoConverter extends DynamoDBPersistentObjectConverter<InsightScheduleInfo, PersistentInsightScheduleInfo>
{
    public PersistentInsightScheduleInfoConverter()
    {
        super(InsightScheduleInfo.class, PersistentInsightScheduleInfo.class);
    }
}
