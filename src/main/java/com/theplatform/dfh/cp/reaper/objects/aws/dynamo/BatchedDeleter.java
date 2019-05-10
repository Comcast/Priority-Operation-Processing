package com.theplatform.dfh.cp.reaper.objects.aws.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchWriteItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteRequest;
import com.amazonaws.services.dynamodbv2.model.ReturnConsumedCapacity;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import com.theplatform.com.dfh.modules.sync.util.Consumer;
import com.theplatform.com.dfh.modules.sync.util.ConsumerResult;
import com.theplatform.com.dfh.modules.sync.util.InstantUtil;
import com.theplatform.dfh.cp.reaper.objects.aws.BaseBatchedOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Batched dynamo deleter.
 *
 * Note: Dynamo has built in retry for issues like capacity.
 */
public class BatchedDeleter extends BaseBatchedOperation implements Consumer<String>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // this is a limit per write batch request on dynamo
    public final static int MAX_BATCH_WRITE_ITEM = 25;

    private final AmazonDynamoDB dynamoDB;
    private final String tableName;
    private final String fieldName;
    private long deleteCallDelayMillis = 0;
    private boolean logDeleteOnly = false;

    public BatchedDeleter(AmazonDynamoDB dynamoDB,
        String tableName,
        String fieldName)
    {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
        this.fieldName = fieldName;
    }

    @Override
    public ConsumerResult<String> consume(Collection<String> collection, Instant endProcessingTime)
    {
        if(collection == null)
        {
            logger.info("Input collection is null. No deletes to perform on {}", tableName);
            return new ConsumerResult<String>().setItemsConsumedCount(0);
        }

        logger.info("Attempting to delete {} items from {}", collection.size(), tableName);

        int currentIndex = 0;
        int itemsRemoved = 0;
        List<String> itemKeys = new LinkedList<>(collection);
        while(currentIndex < itemKeys.size())
        {
            int lastIndex = Math.min(currentIndex + MAX_BATCH_WRITE_ITEM, itemKeys.size());
            List<String> deleteBatch = itemKeys.subList(currentIndex, lastIndex);
            try
            {
                itemsRemoved += deleteItems(deleteBatch);
            }
            catch(Exception e)
            {
                logger.error(String.format("Delete operation failed on %1$s. Interrupting processing.", tableName), e);
                return new ConsumerResult<String>().setInterrupted(true);
            }
            currentIndex += MAX_BATCH_WRITE_ITEM;

            if(InstantUtil.isNowAfterOrEqual(endProcessingTime))
                break;

            if(!delay(deleteCallDelayMillis))
                break;
        }
        logger.info("Deleted {} items from {}", tableName, itemsRemoved);
        return new ConsumerResult<String>().setItemsConsumedCount(itemsRemoved);
    }

    protected int deleteItems(Collection<String> idsToDelete)
    {
        if(idsToDelete != null && idsToDelete.size() > 0)
        {
            BatchWriteItemRequest batchWriteItemRequest = new BatchWriteItemRequest()
                .withRequestItems(createRequestItemMap(idsToDelete))
                .withReturnConsumedCapacity(ReturnConsumedCapacity.TOTAL);

            // do not perform the delete
            if(logDeleteOnly)
            {
                logger.info("[Log Only Mode] Would delete the following ids from {}: {}", tableName, String.join(",", idsToDelete));
                return idsToDelete.size();
            }

            logger.info("Deleting the following ids from {}: {}", tableName, String.join(",", idsToDelete));
            BatchWriteItemResult batchWriteItemResult = dynamoDB.batchWriteItem(batchWriteItemRequest);
            if(batchWriteItemResult.getUnprocessedItems() != null
                && batchWriteItemResult.getUnprocessedItems().containsKey(tableName))
            {
                List<WriteRequest> unprocessedItems = batchWriteItemResult.getUnprocessedItems().get(tableName);
                logUnprocessedIds(unprocessedItems);
                return idsToDelete.size() - unprocessedItems.size();
            }
            return idsToDelete.size();
        }
        return 0;
    }

    protected void logUnprocessedIds(List<WriteRequest> unprocessedItems)
    {
        List<String> unprocessedIds =
            unprocessedItems.stream()
                .filter(item ->
                    item.getDeleteRequest() != null
                    && item.getDeleteRequest().getKey() != null
                    && item.getDeleteRequest().getKey().containsKey(fieldName)
                )
                .map(i -> i.getDeleteRequest().getKey().get(fieldName).getS())
                .collect(Collectors.toList());

        if(unprocessedIds.size() > 0)
            logger.info("Failed to delete the following ids from {}: {}", tableName, String.join(",", unprocessedIds));

    }

    protected Map<String, List<WriteRequest>> createRequestItemMap(Collection<String> itemsToDelete)
    {
        // create a list of the items to request the deletes
        List<WriteRequest> requestItems = itemsToDelete.stream().map(
            fieldValue ->
                new WriteRequest()
                    .withDeleteRequest(new DeleteRequest().addKeyEntry(fieldName, new AttributeValue().withS(fieldValue)))
        ).collect(Collectors.toList());

        return Collections.singletonMap(tableName, requestItems);

    }

    public BatchedDeleter setDeleteCallDelayMillis(long deleteCallDelayMillis)
    {
        this.deleteCallDelayMillis = deleteCallDelayMillis;
        return this;
    }

    public BatchedDeleter setLogDeleteOnly(boolean logDeleteOnly)
    {
        this.logDeleteOnly = logDeleteOnly;
        return this;
    }

    @Override
    public String toString()
    {
        return "BatchedDeleter{" +
            "dynamoDB=" + dynamoDB +
            ", tableName='" + tableName + '\'' +
            ", fieldName='" + fieldName + '\'' +
            ", deleteCallDelayMillis=" + deleteCallDelayMillis +
            ", logDeleteOnly=" + logDeleteOnly +
            '}';
    }
}