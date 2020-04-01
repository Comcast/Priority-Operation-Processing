package com.comcast.fission.endpoint.test.facility;

import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.api.data.query.resourcepool.insight.ByResourcePoolId;
import com.comcast.fission.endpoint.test.BaseEndpointObjectClientTest;
import com.comcast.fission.endpoint.test.factory.DataGenerator;
import com.theplatform.dfh.cp.api.facility.Insight;
import com.comcast.fission.endpoint.test.messages.ValidationExceptionMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.UUID;

public class InsightEndpointClientTest extends BaseEndpointObjectClientTest<Insight>
{
    private final String UPDATED_OWNER_ID = "theUpdate";
    private static final String CLASS_FIELD = "class";

    private static final Logger logger = LoggerFactory.getLogger(InsightEndpointClientTest.class);

    public InsightEndpointClientTest()
    {
        super(Insight.class);
    }

    @Override
    public String getEndpointUrl()
    {
        return insightUrl;
    }

    @Override
    protected Insight getTestObject()
    {
        Insight insight = DataGenerator.generateInsight(testCustomerId);
        return insight;
    }

    @Override
    protected Insight updateTestObject(Insight object)
    {
        object.setQueueName(UPDATED_OWNER_ID);
        return object;
    }

    @Test
    public void testByInsightId()
    {
        final String INSIGHT_ID = "this-is-a-test-" + UUID.randomUUID();
        Insight insight = DataGenerator.generateInsight(testCustomerId);
        insight.setTitle(INSIGHT_ID);
        insight.setResourcePoolId(INSIGHT_ID);
        DataObjectResponse<Insight> response = insightClient.persistObject(insight);
        Assert.assertFalse(response.isError());
        response = insightClient.getObjects(Collections.singletonList(new ByResourcePoolId(INSIGHT_ID)));
        Assert.assertFalse(response.isError());
        Assert.assertEquals(response.getCount(), new Integer(1));
        Assert.assertEquals(response.getFirst().getTitle(), INSIGHT_ID);
        Assert.assertEquals(response.getFirst().getResourcePoolId(), INSIGHT_ID);
    }

    @Test
    public void testMissingTitleField()
    {
        Insight insight = DataGenerator.generateInsight(testCustomerId);
        insight.setTitle(null);
        verifyValidationExceptionOnPersist(insight, ValidationExceptionMessage.TITLE_NOT_SPECIFIED);
    }

    @Override
    protected void verifyUpdatedTestObject(Insight object)
    {
        Assert.assertEquals(object.getQueueName(), UPDATED_OWNER_ID);
    }

    @Override
    protected void verifyCreatedTestObject(Insight createdObject, Insight testObject)
    {
        assertEqual(createdObject, testObject);
    }

    @Test
    public void testSparseUpdate()
    {
        Insight testObject = getTestObject();
        testObject.setIsGlobal(false);
        testObject.setQueueSize(5);
        DataObjectResponse<Insight> createdObjectResponse = insightClient.persistObject(testObject);
        Assert.assertFalse(createdObjectResponse.isError(), String.format("Unexpected error: %1$s", createdObjectResponse.getErrorResponse()));
        Insight created = createdObjectResponse.getFirst();

        // do sparse update and verify unspecified fields still match
        Insight toUpdate = new Insight();
        toUpdate.setId(created.getId());
        toUpdate.setQueueName(RandomStringUtils.random(10));
        DataObjectResponse<Insight> updateObjectResponse = insightClient.updateObject(toUpdate, toUpdate.getId());
        Assert.assertFalse(updateObjectResponse.isError(), String.format("Unexpected error: %1$s", updateObjectResponse.getErrorResponse()));
        Insight updated = updateObjectResponse.getFirst();

        Assert.assertEquals(updated.isGlobal(), created.isGlobal(), "Insight.isGlobal did not match after sparse update.");
        Assert.assertEquals(updated.getQueueSize(), created.getQueueSize(), "Insight.queueSize did not match after sparse update.");
    }
}
