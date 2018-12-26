package com.theplatform.dfh.endpoint.client;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.query.Query;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class HttpCPObjectClientTest
{
    private Class<?> clazz = AgendaProgress.class;
    private HttpCPObjectClient<AgendaProgress> client;
    private HttpURLConnectionFactory mockHttpURLConnectionFactory;
    private HttpURLConnection mockHttpURLConnection;
    private URLRequestPerformer mockURLRequestPerformer;
    private JsonHelper jsonHelper = new JsonHelper();
    private final String LINK_ID = UUID.randomUUID().toString();

    @BeforeMethod
    public void setup() throws Exception
    {
        mockURLRequestPerformer = mock(URLRequestPerformer.class);
        mockHttpURLConnection = mock(HttpURLConnection.class);
        mockHttpURLConnectionFactory = mock(HttpURLConnectionFactory.class);
        doReturn(mockHttpURLConnection).when(mockHttpURLConnectionFactory).getHttpURLConnection(anyString());
        client = new HttpCPObjectClient<>("", mockHttpURLConnectionFactory ,clazz);
        client.setUrlRequestPerformer(mockURLRequestPerformer);
    }

    @Test
    public void testGetObjects() throws Exception
    {
        final int ITEM_COUNT = 3;
        doReturn(getAgendaProgressJson(ITEM_COUNT)).when(mockURLRequestPerformer).performURLRequest(any(), any());
        DataObjectFeed<AgendaProgress> dataObjectFeed = client.getObjects("");
        Assert.assertEquals(dataObjectFeed.getAll().size(), ITEM_COUNT);
        // This is necessary as jackson issues were observed
        Assert.assertEquals(dataObjectFeed.getAll().get(0).getClass(), AgendaProgress.class);
    }

    private String getAgendaProgressJson(int count) throws Exception
    {
        DataObjectFeed<AgendaProgress> dataObjectFeed = new DataObjectFeed<>();
        IntStream.range(0, count)
            .forEach(i ->
            {
                AgendaProgress agendaProgress = new AgendaProgress();
                agendaProgress.setId(UUID.randomUUID().toString());
                agendaProgress.setLinkId(LINK_ID);
                dataObjectFeed.add(agendaProgress);
            });
        return jsonHelper.getJSONString(dataObjectFeed);
    }

    @DataProvider
    public Object[][] queryParamsProvider()
    {
        return new Object[][]
            {
                { "" , Collections.emptyList() },
                { "?byX=1" , Collections.singletonList(makeQuery("X","1")) },
                { "?byX=1&byY=2" , Arrays.asList(makeQuery("X","1"), makeQuery("Y","2")) }
            };
    }

    @Test(dataProvider = "queryParamsProvider")
    public void testGetQueryParams(final String expected, Collection<Query> queries)
    {
        Assert.assertEquals(client.getQueryParams(queries), expected);
    }

    private Query makeQuery(String field, String value)
    {
        return new Query<>(field, value);
    }
}
