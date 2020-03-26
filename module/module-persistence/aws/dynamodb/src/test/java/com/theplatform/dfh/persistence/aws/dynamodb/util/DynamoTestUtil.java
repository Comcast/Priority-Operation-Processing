package com.theplatform.dfh.persistence.aws.dynamodb.util;

import com.amazonaws.services.dynamodbv2.datamodeling.QueryResultPage;
import com.amazonaws.services.dynamodbv2.datamodeling.ScanResultPage;
import com.theplatform.dfh.persistence.aws.dynamodb.TestTrackedObject;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DynamoTestUtil
{
    // Thanks AWS for not sharing the paging stuff in any way
    public static QueryResultPage<TestTrackedObject> createQueryResultPage(boolean hasAdditionalPages, Integer items)
    {
        QueryResultPage<TestTrackedObject> resultPage = new QueryResultPage<>();
        resultPage.setResults(
            IntStream.range(0, items).mapToObj(i ->
            {
                return new TestTrackedObject();
            }).collect(Collectors.toList())
        );
        if(hasAdditionalPages)
            resultPage.setLastEvaluatedKey(new HashMap<>());
        return resultPage;
    }

    public static ScanResultPage<TestTrackedObject> createScanResultPage(boolean hasAdditionalPages, Integer items)
    {
        ScanResultPage<TestTrackedObject> resultPage = new ScanResultPage<>();
        resultPage.setResults(
            IntStream.range(0, items).mapToObj(i ->
            {
                return new TestTrackedObject();
            }).collect(Collectors.toList())
        );
        if(hasAdditionalPages)
            resultPage.setLastEvaluatedKey(new HashMap<>());
        return resultPage;
    }
}
