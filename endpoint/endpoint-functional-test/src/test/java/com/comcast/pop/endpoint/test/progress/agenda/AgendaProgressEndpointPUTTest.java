package com.comcast.pop.endpoint.test.progress.agenda;

import com.comcast.pop.endpoint.test.base.EndpointTestBase;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.api.progress.ProcessingState;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * This class covers direct put to the agenda progress endpoint (the other calls /{object id)}
 */
public class AgendaProgressEndpointPUTTest extends EndpointTestBase
{
    private final String EXTERNAL_ID = "http://theexternal.id";
    private URLRequestPerformer urlRequestPerformer;

    @BeforeMethod
    public void setup()
    {
        urlRequestPerformer = new URLRequestPerformer();
    }

    @Test
    public void testDirectPUT()
    {
        final String PROCESSING_MESSAGE = "processingMessage";
        final ProcessingState PROCESSING_STATE = ProcessingState.EXECUTING;

        // persist
        AgendaProgress agendaProgress = getTestObject();
        agendaProgress = agendaProgressClient.persistObject(agendaProgress).getFirst();

        // update
        AgendaProgress sparseProgress = new AgendaProgress();
        sparseProgress.setId(agendaProgress.getId());
        sparseProgress.setProcessingState(PROCESSING_STATE);
        sparseProgress.setProcessingStateMessage(PROCESSING_MESSAGE);
        reportProgress(sparseProgress);
        AgendaProgress updatedProgress = agendaProgressClient.getObject(agendaProgress.getId()).getFirst();

        // confirm update
        Assert.assertEquals(updatedProgress.getProcessingStateMessage(), PROCESSING_MESSAGE);
        Assert.assertEquals(updatedProgress.getProcessingState(), PROCESSING_STATE);

        // confirm other fields were not changed
        Assert.assertEquals(updatedProgress.getExternalId(), agendaProgress.getExternalId());
        Assert.assertEquals(updatedProgress.getLinkId(), agendaProgress.getLinkId());
        Assert.assertTrue(updatedProgress.getParams().containsKey(GeneralParamKey.externalId));
    }

    protected AgendaProgress getTestObject()
    {
        AgendaProgress progress = new AgendaProgress();
        progress.setCustomerId(testCustomerId);
        progress.setLinkId("56789");
        progress.setExternalId(EXTERNAL_ID);
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(GeneralParamKey.externalId, EXTERNAL_ID);
        progress.setParams(paramsMap);
        return progress;
    }

    public void reportProgress(AgendaProgress agendaProgress)
    {
        try
        {
            byte[] data = jsonHelper.getJSONString(agendaProgress).getBytes();

            HttpURLConnection urlConnection = getDefaultHttpURLConnectionFactory().getHttpURLConnection(
                agendaProgressUrl,
                "application/json",
                data);
            urlConnection.setRequestMethod("PUT");
            urlConnection.setConnectTimeout(30000);
            urlRequestPerformer.performURLRequest(urlConnection, data);
        }
        catch (IOException e)
        {
            throw new RuntimeException(String.format("Failed to update progress: %1$s", agendaProgressUrl), e);
        }
    }
}
