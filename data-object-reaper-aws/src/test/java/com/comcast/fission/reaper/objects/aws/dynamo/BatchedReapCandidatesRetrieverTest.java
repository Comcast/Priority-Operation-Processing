package com.comcast.fission.reaper.objects.aws.dynamo;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.theplatform.com.dfh.modules.sync.util.ProducerResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BatchedReapCandidatesRetrieverTest
{
    private AmazonDynamoDB mockAmazonDynamoDB;
    private BatchedReapCandidatesRetriever retriever;
    final String ID_FIELD = "id";

    @BeforeMethod
    public void setup()
    {
        mockAmazonDynamoDB = mock(AmazonDynamoDB.class);
        retriever = new BatchedReapCandidatesRetriever(mockAmazonDynamoDB, "", ID_FIELD, "", 0L);
    }

    @Test
    public void testReset()
    {
        ScanResult scanResult = new ScanResult();
        scanResult.setLastEvaluatedKey(null);
        doReturn(scanResult).when(mockAmazonDynamoDB).scan(any(ScanRequest.class));
        Assert.assertNotNull(retriever.produce(Instant.now().plusSeconds(60)));
        verify(mockAmazonDynamoDB, times(1)).scan(any(ScanRequest.class));

        // confirm that a second call doesn't increase the calls to dynamo
        Assert.assertNotNull(retriever.produce(Instant.now().plusSeconds(60)));
        verify(mockAmazonDynamoDB, times(1)).scan(any(ScanRequest.class));

        retriever.reset();
        Assert.assertNotNull(retriever.produce(Instant.now().plusSeconds(60)));
        verify(mockAmazonDynamoDB, times(2)).scan(any(ScanRequest.class));
    }

    @DataProvider
    public Object[][] produceDesiredRequestCountProvider()
    {
        return new Object[][]
            {
                { 0 },
                { 100 }
            };
    }

    @Test(dataProvider = "produceDesiredRequestCountProvider")
    public void testProduceToDesiredLimit(final int desiredRequestCount)
    {
        doReturn(createScanResult(1, false)).when(mockAmazonDynamoDB).scan(any(ScanRequest.class));
        retriever.setTargetBatchSize(desiredRequestCount);
        Assert.assertTrue(retriever.produce(Instant.now().plusSeconds(60)).getItemsProduced().size() >= desiredRequestCount);
    }

    @Test
    public void testEndProcessingTime()
    {
        doReturn(createScanResult(1, false)).when(mockAmazonDynamoDB).scan(any(ScanRequest.class));
        Assert.assertEquals(retriever.produce(Instant.now().minusSeconds(60)).getItemsProduced().size(), 1);
    }

    @Test
    public void testDynamoExceptionAsInterrupt()
    {
        doThrow(new RuntimeException("FAIL")).when(mockAmazonDynamoDB).scan(any(ScanRequest.class));
        ProducerResult<String> producerResult = retriever.produce(Instant.now().plusSeconds(60));
        Assert.assertTrue(producerResult.isInterrupted());
    }

    protected ScanResult createScanResult(int itemCount, boolean lastEntry)
    {
        List<Map<String, AttributeValue>> rows = new LinkedList<>();
        IntStream.range(0, itemCount).forEach(
            i -> rows.add(Collections.singletonMap(ID_FIELD, new AttributeValue()))
        );

        ScanResult scanResult = new ScanResult();
        scanResult.setLastEvaluatedKey(new HashMap<>());
        scanResult.setItems(rows);
        scanResult.setLastEvaluatedKey(
            lastEntry
            ? null
            : new HashMap<>()
        );
        scanResult.setCount(scanResult.getItems().size());
        return scanResult;
    }
}
