package com.theplatform.dfh.cp.agenda.reclaim.aws.dynamo;

import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.dfh.cp.agenda.reclaim.aws.config.AWSReclaimerConfig;
import com.theplatform.dfh.cp.agenda.reclaim.factory.AgendaProgressProducerFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;

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
