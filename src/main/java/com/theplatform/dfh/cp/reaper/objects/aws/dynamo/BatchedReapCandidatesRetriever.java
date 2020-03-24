package com.theplatform.dfh.cp.reaper.objects.aws.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Retriever for reap candidates (those with a given UTC time field that is lower than the specified bound)
 */
public class BatchedReapCandidatesRetriever extends BatchedObjectFieldRetriever
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String timeFieldName;
    private final long reapUpperBoundUTC;

    public BatchedReapCandidatesRetriever(AmazonDynamoDB dynamoDB,
        String tableName,
        String idField,
        String timeFieldName,
        long reapUpperBoundUTC)
    {
        super(dynamoDB, tableName, idField);
        this.timeFieldName = timeFieldName;
        this.reapUpperBoundUTC = reapUpperBoundUTC;
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
        return new ScanRequest()
            .withTableName(tableName)
            .withAttributesToGet(idField)
            .withConsistentRead(true)
            .withExclusiveStartKey(startKey)
            .withLimit(maximumItemEvaluation)
            .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL)
            // this filter is timeFieldName.value < reapUpperBound
            .withScanFilter(
                Collections.singletonMap(
                    timeFieldName,
                    new Condition()
                        .withComparisonOperator(ComparisonOperator.LT)
                        .withAttributeValueList(new AttributeValue().withN(String.valueOf(reapUpperBoundUTC)))));
    }

    @Override
    public String toString()
    {
        return "BatchedReapCandidatesRetriever{" +
            "timeFieldName='" + timeFieldName + '\'' +
            ", reapUpperBoundUTC=" + reapUpperBoundUTC +
            "} " + super.toString();
    }
}
