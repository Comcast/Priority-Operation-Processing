package com.comcast.pop.agenda.reclaim.aws.dynamo;

import com.comcast.pop.modules.sync.util.Producer;
import com.comcast.pop.agenda.reclaim.aws.config.AWSReclaimerConfig;
import com.comcast.pop.agenda.reclaim.factory.AgendaProgressProducerFactory;
import com.comcast.pop.persistence.aws.dynamodb.AWSDynamoDBFactory;

import java.time.Instant;

public class DynamoDBTimeoutProducerFactory implements AgendaProgressProducerFactory
{
    private final AWSDynamoDBFactory awsDynamoDBFactory;
    private final AWSReclaimerConfig config;

    public DynamoDBTimeoutProducerFactory(AWSDynamoDBFactory awsDynamoDBFactory, AWSReclaimerConfig config)
    {
        this.awsDynamoDBFactory = awsDynamoDBFactory;
        this.config = config;
    }

    @Override
    public Producer<String> create()
    {
        return new AgendaProgressTimeoutProducer(
            awsDynamoDBFactory.getAmazonDynamoDB(),
            config.getTableName(),
            config.getIdFieldName(),
            config.getTimeFieldName(),
            Instant.now().minusSeconds(config.getReclaimAgeMinutes() * 60).toEpochMilli()
        )
        .setObjectScanLimit(config.getObjectScanLimit())
        .setScanDelayMillis(config.getScanDelayMillis())
        .setTargetBatchSize(config.getTargetBatchSize());
    }
}
