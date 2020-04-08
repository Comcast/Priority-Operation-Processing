package com.comcast.pop.cp.endpoint.resourcepool;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.api.facility.ResourcePool;
import com.comcast.pop.cp.endpoint.AbstractRequestProcessorTest;
import com.comcast.pop.endpoint.base.DataObjectRequestProcessor;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.object.api.IdentifiedObject;
import com.comcast.pop.persistence.api.ObjectPersister;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ResourcePoolRequestProcessorTest extends AbstractRequestProcessorTest<ResourcePool>
{
    private InsightRequestProcessor mockInsightClient;

    @BeforeMethod
    public void setup()
    {
        mockInsightClient = mock(InsightRequestProcessor.class);
    }

    @Override
    public ResourcePool getDataObject()
    {
        ResourcePool pool = new ResourcePool();
        return pool;
    }

    public DataObjectRequestProcessor<ResourcePool> getRequestProcessor(ObjectPersister<ResourcePool> persister)
    {
        ResourcePoolRequestProcessor processor = new ResourcePoolRequestProcessor(persister, null);
        processor.setInsightClient(mockInsightClient);
        return processor;
    }

    @Test
    public void testAddInsightIdsResourcePoolLookupError()
    {
        DataObjectResponse<ResourcePool> response = generateErrorResponse(ResourcePool.class);
        ((ResourcePoolRequestProcessor)getRequestProcessor(getPersister())).addInsightIds(response);
        verify(mockInsightClient, times(0)).handleGET(any());
    }

    @DataProvider
    public Object[][] insightIdCountProvider()
    {
        return new Object[][]
        {
            { new int[]{0} },
            { new int[]{0, 0, 0, 0} },
            { new int[]{1} },
            { new int[]{1, 3, 8, 9} },
            { new int[]{1, 3, 0, 9} }
        };
    }

    @Test(dataProvider = "insightIdCountProvider")
    public void testAddInsightIdsSuccess(int[] insightsPerResourcePoolCount)
    {
        DataObjectResponse<ResourcePool> resourcePoolResponse = generateResponse(insightsPerResourcePoolCount.length, ResourcePool.class);
        doAnswer(new Answer()
        {
            private int callCount = 0;
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable
            {
                return generateResponse(insightsPerResourcePoolCount[callCount++], Insight.class);
            }
        }).when(mockInsightClient).handleGET(any());
        ((ResourcePoolRequestProcessor)getRequestProcessor(getPersister())).addInsightIds(resourcePoolResponse);
        Assert.assertFalse(resourcePoolResponse.isError());
        Assert.assertEquals(resourcePoolResponse.getCount(), new Integer(insightsPerResourcePoolCount.length));
        // confirm the insight ids are present
        for(int idx = 0; idx < resourcePoolResponse.getAll().size(); idx++)
        {
            Assert.assertEquals(
                resourcePoolResponse.getAll().get(idx).getInsightIds().size(),
                insightsPerResourcePoolCount[idx]);
            for(int id = 0; id < insightsPerResourcePoolCount[idx]; id++)
            {
                Assert.assertTrue(resourcePoolResponse.getAll().get(idx).getInsightIds().contains(Integer.toString(id)),
                    String.format("ResourcePool: %1$s is missing id: %2$s", idx, id));
            }
        }
    }

    @Test
    public void testAddInsightIdsError()
    {
        DataObjectResponse<ResourcePool> resourcePoolResponse = generateResponse(1, ResourcePool.class);
        DataObjectResponse<Insight> insightErrorResponse = generateErrorResponse(Insight.class);
        doReturn(insightErrorResponse).when(mockInsightClient).handleGET(any());
        ((ResourcePoolRequestProcessor)getRequestProcessor(getPersister())).addInsightIds(resourcePoolResponse);
        Assert.assertTrue(resourcePoolResponse.isError());
    }

    private <T extends IdentifiedObject> DataObjectResponse<T> generateResponse(int count, Class<T> clazz)
    {
        DataObjectResponse<T> response = new DefaultDataObjectResponse<>();
        IntStream.range(0, count).forEach(i ->
        {
            try
            {
                T obj = clazz.newInstance();
                obj.setId(Integer.toString(i));
                response.add(obj);
            }
            catch(Exception e)
            {
                Assert.fail("Failed to initialize class: " + clazz.getSimpleName());
            }
        });
        return response;
    }

    private <T extends IdentifiedObject> DataObjectResponse<T> generateErrorResponse(Class<T> clazz)
    {
        DataObjectResponse<T> response = new DefaultDataObjectResponse<>();
        response.setErrorResponse(ErrorResponseFactory.badRequest("Failure to lookup " + clazz.getSimpleName(), UUID.randomUUID().toString()));
        return response;
    }
}
