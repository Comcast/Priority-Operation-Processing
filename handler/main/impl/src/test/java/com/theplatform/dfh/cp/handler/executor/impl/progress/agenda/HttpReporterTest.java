package com.theplatform.dfh.cp.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.HttpURLConnection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class HttpReporterTest
{
    private HttpReporter httpReporter;
    private final String EXCEPTION_MESSAGE = "theError";

    private HttpURLConnectionFactory mockHttpURLConnectionFactory;
    private URLRequestPerformer mockUrlRequestPerformer;

    @BeforeMethod
    public void setup() throws Exception
    {
        httpReporter = new HttpReporter();
        mockHttpURLConnectionFactory = mock(HttpURLConnectionFactory.class);
        mockUrlRequestPerformer = mock(URLRequestPerformer.class);
        doReturn(mock(HttpURLConnection.class)).when(mockHttpURLConnectionFactory).getHttpURLConnection(any(), any(), any(), any());
        httpReporter.setHttpURLConnectionFactory(mockHttpURLConnectionFactory);
        httpReporter.setUrlRequestPerformer(mockUrlRequestPerformer);
    }

    @Test
    public void testSuccessfulReport()
    {
        httpReporter.reportProgress(new TestObject());
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = EXCEPTION_MESSAGE)
    public void testFailedReport() throws Exception
    {
        doThrow(new RuntimeException(EXCEPTION_MESSAGE)).when(mockUrlRequestPerformer).performURLRequest(any(), any());
        httpReporter.reportProgress(new TestObject());
    }

    private class TestObject
    {
        private String id;

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }
    }
}
