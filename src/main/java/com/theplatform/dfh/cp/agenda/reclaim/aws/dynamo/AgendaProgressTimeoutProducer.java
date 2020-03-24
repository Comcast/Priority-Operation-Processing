package com.theplatform.dfh.cp.agenda.reclaim.aws.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.reaper.objects.aws.dynamo.BatchedObjectFieldRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AgendaProgressTimeoutProducer extends BatchedObjectFieldRetriever
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String SEEK_STATE_VALUE = ProcessingState.EXECUTING.name();
    private static final String STATE_FIELD = "processingState";

    private final String timeFieldName;
    private final long reclaimUpperBoundUTC;

    public AgendaProgressTimeoutProducer(
        AmazonDynamoDB dynamoDB,
        String tableName,
        String idField,
        String timeFieldName,
        long reclaimUpperBoundUTC)
    {
        super(dynamoDB, tableName, idField);
        this.timeFieldName = timeFieldName;
        this.reclaimUpperBoundUTC = reclaimUpperBoundUTC;
    }

    /**
     * Creates a scan spec for the reaping scan
     * @param tableName The name of the table to scan
     * @param startKey The starting key (may be null)
     * @param idField The field of the id to retrieve
     * @param maximumItemEvaluation The maximum number of results to scan for
     * @return ScanRequest with the specified settings
     */
    @Override
    protected ScanRequest createScanRequest(String tableName, Map<String, AttributeValue> startKey,
        String idField, int maximumItemEvaluation)
    {
        Map<String, Condition> scanFilters = new HashMap<>();
        // timeFieldName.value < reapUpperBound
        scanFilters.put(
            timeFieldName,
            new Condition()
                .withComparisonOperator(ComparisonOperator.LT)
                .withAttributeValueList(new AttributeValue().withN(String.valueOf(reclaimUpperBoundUTC)))
        );
        // state field = state value
        scanFilters.put(
            STATE_FIELD,
            new Condition()
                .withComparisonOperator(ComparisonOperator.EQ)
                .withAttributeValueList(new AttributeValue().withS(SEEK_STATE_VALUE))
        );

        return new ScanRequest()
            .withTableName(tableName)
            .withAttributesToGet(idField)
            .withConsistentRead(true)
            .withExclusiveStartKey(startKey)
            .withLimit(maximumItemEvaluation)
            .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
            .withScanFilter(scanFilters);
    }
}
