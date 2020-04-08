package com.comcast.pop.endpoint.client;

import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import com.comcast.pop.endpoint.api.ErrorResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.http.api.HttpURLConnectionFactory;
import com.comcast.pop.http.util.URLRequestPerformer;
import com.comcast.pop.persistence.api.DataObjectFeed;
import com.comcast.pop.persistence.api.query.Query;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
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

public class HttpObjectClientTest
{
    private Class<AgendaProgress> clazz = AgendaProgress.class;
    private HttpObjectClient<AgendaProgress> client;
    private HttpURLConnectionFactory mockHttpURLConnectionFactory;
    private HttpURLConnection mockHttpURLConnection;
    private URLRequestPerformer mockURLRequestPerformer;
    private JsonHelper jsonHelper = new JsonHelper();
    private final String HTTP_RESPONSE_MESSAGE = "theMessage";
    private final String LINK_ID = UUID.randomUUID().toString();

    @BeforeMethod
    public void setup() throws Exception
    {
        mockURLRequestPerformer = mock(URLRequestPerformer.class);
        mockHttpURLConnection = mock(HttpURLConnection.class);
        mockHttpURLConnectionFactory = mock(HttpURLConnectionFactory.class);
        doReturn(mockHttpURLConnection).when(mockHttpURLConnectionFactory).getHttpURLConnection(anyString());
        client = new HttpObjectClient<>("", mockHttpURLConnectionFactory, clazz);
        client.setUrlRequestPerformer(mockURLRequestPerformer);
    }

    @Test
    public void testGetObjects() throws Exception
    {
        final int ITEM_COUNT = 3;
        doReturn(getAgendaProgressJson(ITEM_COUNT)).when(mockURLRequestPerformer).performURLRequest(any(), any());
        DataObjectResponse<AgendaProgress> dataObjectFeed = client.getObjects("");
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

    @DataProvider
    public Object[][] buildExceptionResponseProvider() throws IOException
    {
        return new Object[][]
            {
                { configureConnectionMock("test", 502), "test :: " + HTTP_RESPONSE_MESSAGE},
                { configureConnectionMock("", 504), " :: " + HTTP_RESPONSE_MESSAGE},
                { configureConnectionMock(null, 408), HttpObjectClient.DEFAULT_RESPONSE_MESSAGE + " :: " + HTTP_RESPONSE_MESSAGE},
            };
    }

    @Test(dataProvider = "buildExceptionResponseProvider")
    public void testBuildExceptionResponse(HttpURLConnection connection, String expectedMessage) throws IOException
    {
        final IOException e = new IOException();
        DataObjectResponse<AgendaProgress> dataObjectResponse = client.buildExceptionResponse(e, HTTP_RESPONSE_MESSAGE, connection);
        Assert.assertNotNull(dataObjectResponse.getErrorResponse());
        Assert.assertTrue(dataObjectResponse.isError());
        ErrorResponse errorResponse = dataObjectResponse.getErrorResponse();
        Assert.assertEquals(errorResponse.getTitle(), ObjectClientException.class.getSimpleName());
        Assert.assertTrue(errorResponse.getServerStackTrace().startsWith(e.getClass().getName()), "StackTrace should have the prefix: " + e.getClass().getName());
        Assert.assertEquals(errorResponse.getResponseCode(), (Integer)connection.getResponseCode());
        Assert.assertEquals(errorResponse.getDescription(), expectedMessage);
    }

    private HttpURLConnection configureConnectionMock(String responseMessage, int responseCode) throws IOException
    {
        // make a new one because BeforeMethod has not run yet...
        HttpURLConnection mockHttpURLConnection = mock(HttpURLConnection.class);
        doReturn(responseMessage).when(mockHttpURLConnection).getResponseMessage();
        doReturn(responseCode).when(mockHttpURLConnection).getResponseCode();
        return mockHttpURLConnection;
    }

    private Query makeQuery(String field, String value)
    {
        return new Query<>(field, value);
    }
}
