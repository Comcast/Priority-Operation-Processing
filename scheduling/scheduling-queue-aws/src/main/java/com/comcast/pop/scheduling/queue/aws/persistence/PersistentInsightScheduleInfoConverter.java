package com.comcast.pop.scheduling.queue.aws.persistence;

import com.comcast.pop.scheduling.queue.InsightScheduleInfo;
import com.comcast.pop.persistence.aws.dynamodb.DynamoDBPersistentObjectConverter;

/**
 */
public class PersistentInsightScheduleInfoConverter extends DynamoDBPersistentObjectConverter<InsightScheduleInfo, PersistentInsightScheduleInfo>
{
    public PersistentInsightScheduleInfoConverter()
    {
        super(InsightScheduleInfo.class, PersistentInsightScheduleInfo.class);
    }
}
