package com.theplatform.dfh.cp.reaper.objects.aws.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.theplatform.com.dfh.modules.sync.util.InstantUtil;
import com.theplatform.com.dfh.modules.sync.util.Producer;
import com.theplatform.com.dfh.modules.sync.util.ProducerResult;
import com.theplatform.dfh.cp.reaper.objects.aws.BaseBatchedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Basic field retriever (scans entire table retrieving the specified field!)
 *
 * Note: Dynamo has built in retry for issues like capacity.
 */
public class BatchedObjectFieldRetriever extends BaseBatchedOperation implements Producer<String>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AmazonDynamoDB dynamoDB;
    private final String tableName;
    private final String fieldName;
    private long scanDelayMillis = 0;
    private int targetBatchSize = 50;
    private int objectScanLimit = 50;

    // this is a dynamodb thing for tracking the last key evaluated (pagination)
    private Map<String, AttributeValue> lastKeyEvaluated;
    private boolean scanComplete;

    public BatchedObjectFieldRetriever(AmazonDynamoDB dynamoDB,
        String tableName,
        String fieldName)
    {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
        this.fieldName = fieldName;

        reset();
    }

    @Override
    public void reset()
    {
        lastKeyEvaluated = null;
        scanComplete = false;
    }

    @Override
    public ProducerResult<String> produce(Instant endProcessingTime)
    {
        if(scanComplete)
            return new ProducerResult<>();

        logger.info("Attempting to scan for approximately {} items.", targetBatchSize);
        LinkedList<String> ids = new LinkedList<>();
        while(ids.size() < targetBatchSize)
        {
            ScanResult scanResult;
            try
            {
                scanResult = dynamoDB.scan(createScanRequest(tableName, lastKeyEvaluated, fieldName, objectScanLimit));
            }
            catch(Exception e)
            {
                logger.error("Scan operation failed. Interrupting processing.", e);
                return new ProducerResult<String>().setInterrupted(true);
            }
            appendItems(ids, fieldName, scanResult);

            lastKeyEvaluated = scanResult.getLastEvaluatedKey();
            if(lastKeyEvaluated == null)
            {
                scanComplete = true;
                break;
            }
            if(InstantUtil.isEqualOrAfter(endProcessingTime))
                break;

            if(!delay(scanDelayMillis))
                break;
        }
        logger.info("Scan produced {} items.", ids.size());
        return new ProducerResult<String>().setItemsProduced(ids);
    }

    /**
     * Appends the ids from the scan result
     * @param ids The collection of ids to append
     * @param idField The id field to look for in the map of resulting fields
     * @param scanResult The result to pull the ids from
     */
    protected static void appendItems(List<String> ids, String idField, ScanResult scanResult)
    {
        if (scanResult == null
            || scanResult.getCount() == null
            || scanResult.getCount() == 0
            || scanResult.getItems() == null)
            return;

        // add every item that has an id key in the map
        ids.addAll(
            scanResult.getItems().stream()
                .filter(items -> items.containsKey(idField))
                .map(items -> items.get(idField).getS()).collect(Collectors.toList()));
    }

    /**
     * Creates a scan spec for reading every object in the table for the specified id
     * @param tableName The name of the table to scan
     * @param startKey The starting key (may be null)
     * @param idField The field of the id to retrieve
     * @param maximumItemEvaluation The maximum number of results to scan for
     * @return ScanRequest with the specified settings
     */
    protected ScanRequest createScanRequest(String tableName, Map<String, AttributeValue> startKey,
        String idField, int maximumItemEvaluation)
    {
        return new ScanRequest()
            .withTableName(tableName)
            .withAttributesToGet(idField)
            .withConsistentRead(true)
            .withExclusiveStartKey(startKey)
            .withLimit(maximumItemEvaluation)
            .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);
    }

    public BatchedObjectFieldRetriever setObjectScanLimit(int objectScanLimit)
    {
        this.objectScanLimit = objectScanLimit;
        return this;
    }

    public BatchedObjectFieldRetriever setTargetBatchSize(int targetBatchSize)
    {
        this.targetBatchSize = targetBatchSize;
        return this;
    }

    public BatchedObjectFieldRetriever setScanDelayMillis(long scanDelayMillis)
    {
        this.scanDelayMillis = scanDelayMillis;
        return this;
    }

    @Override
    public String toString()
    {
        return "BatchedObjectFieldRetriever{" +
            "dynamoDB=" + dynamoDB +
            ", tableName='" + tableName + '\'' +
            ", fieldName='" + fieldName + '\'' +
            ", scanDelayMillis=" + scanDelayMillis +
            ", targetBatchSize=" + targetBatchSize +
            ", objectScanLimit=" + objectScanLimit +
            ", lastKeyEvaluated=" + lastKeyEvaluated +
            ", scanComplete=" + scanComplete +
            '}';
    }
}
