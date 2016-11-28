package com.theplatform.module.docker;

import com.theplatform.module.docker.elastic.demand.DemandType;
import com.theplatform.module.docker.elastic.demand.HandlerDemandFactory;
import com.theplatform.module.docker.elastic.demand.S3DemandClient;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class HandlerDemandFactoryTest
{
    @Test
    public void testDefaultDemand() throws Exception
    {
        HandlerDemandFactory handlerDemandFactory = new HandlerDemandFactory();
        Assert.assertFalse(handlerDemandFactory.isDemandPresent());
    }

    @Test(expectedExceptions = com.amazonaws.AmazonClientException.class)
    public void testBogusJson() throws Exception
    {
        HandlerDemandFactory handlerDemandFactory = new HandlerDemandFactory();
        S3DemandClient s3DemandClient = mock(S3DemandClient.class);
        String json = "";

        when(s3DemandClient.getDemandJson()).thenReturn(json);
        handlerDemandFactory.setS3DemandClient(s3DemandClient);

        // throws
        handlerDemandFactory.isDemandPresent();
    }

    @Test
    public void testBogusLessDemand() throws Exception
    {
        HandlerDemandFactory handlerDemandFactory = new HandlerDemandFactory();
        S3DemandClient s3DemandClient = mock(S3DemandClient.class);
        String json = "{\"demand\":\" " + DemandType.less + "\"}";

        when(s3DemandClient.getDemandJson()).thenReturn(json);
        handlerDemandFactory.setS3DemandClient(s3DemandClient);
        Assert.assertFalse(handlerDemandFactory.isDemandPresent());
    }

    @Test
    public void testBogusMoreDemand() throws Exception
    {
        HandlerDemandFactory handlerDemandFactory = new HandlerDemandFactory();
        S3DemandClient s3DemandClient = mock(S3DemandClient.class);
        String json = "{\"demand\":\" " + DemandType.more + "\"}";

        when(s3DemandClient.getDemandJson()).thenReturn(json);
        handlerDemandFactory.setS3DemandClient(s3DemandClient);
        Assert.assertFalse(handlerDemandFactory.isDemandPresent());
    }
}


