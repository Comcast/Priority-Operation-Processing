package com.theplatform.dfh.cp.agenda.reclaim.aws.producer;

import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.dfh.cp.agenda.reclaim.aws.config.AWSReclaimerConfig;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.agenda.reclaim.factory.AgendaProgressProducerFactory;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;

import java.time.Instant;

public class DynamoDBTimeoutProducerFactory implements AgendaProgressProducerFactory<AWSReclaimerConfig>
{
    private final AWSDynamoDBFactory awsDynamoDBFactory;

    public DynamoDBTimeoutProducerFactory(AWSDynamoDBFactory awsDynamoDBFactory)
    {
        this.awsDynamoDBFactory = awsDynamoDBFactory;
    }

    @Override
    public Producer<String> create(AWSReclaimerConfig config)
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
