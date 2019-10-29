package com.theplatform.dfh.cp.handler.executor.impl.progress.agenda;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.reporter.api.ProgressReporter;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.http.util.URLRequestPerformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * Http specific Reporter (special reporter for the AgendaProgress)
 */
public class HttpAgendaProgressReporter implements ProgressReporter<AgendaProgress>
{
    private String reportingUrl;
    private String proxyHost;
    private String proxyPort;
    private HttpURLConnectionFactory httpURLConnectionFactory;
    private URLRequestPerformer urlRequestPerformer;
    private JsonHelper jsonHelper = new JsonHelper();
    private int connectionTimeoutMilliseconds = 30000;
    private int readTimeoutMilliseconds = 30000;
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void reportProgress(AgendaProgress agendaProgress)
    {
        try
        {
            logger.debug("Reporting progress");
            byte[] data = jsonHelper.getJSONString(agendaProgress).getBytes();

            logger.debug("Connection info: [" + reportingUrl + "] with proxy [" + proxyHost + ":" + proxyPort + "]");

            Proxy proxy = null;
            if (this.proxyHost != null && this.proxyPort != null)
            {
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.valueOf(this.proxyPort)));
            }

            HttpURLConnection urlConnection = httpURLConnectionFactory.getHttpURLConnection(
                reportingUrl,
                proxy,
                "application/json",
                data);
            urlConnection.setRequestMethod("PUT");
            urlConnection.setConnectTimeout(connectionTimeoutMilliseconds);
            urlConnection.setReadTimeout(readTimeoutMilliseconds);
            urlRequestPerformer.performURLRequest(urlConnection, data);
            logger.info("Successfully reported progress");
        }
        catch(IOException e)
        {
            throw new RuntimeException(String.format("Failed to report progress to endpoint: %1$s", reportingUrl), e);
        }
    }

    @Override
    public void reportProgress(AgendaProgress agendaProgress, Object resultPayload)
    {
        throw new UnsupportedOperationException();
    }

    public int getConnectionTimeoutMilliseconds()
    {
        return connectionTimeoutMilliseconds;
    }

    public HttpAgendaProgressReporter setConnectionTimeoutMilliseconds(int connectionTimeoutMilliseconds)
    {
        this.connectionTimeoutMilliseconds = connectionTimeoutMilliseconds;
        return this;
    }

    public int getReadTimeoutMilliseconds()
    {
        return readTimeoutMilliseconds;
    }

    public void setReadTimeoutMilliseconds(int readTimeoutMilliseconds)
    {
        this.readTimeoutMilliseconds = readTimeoutMilliseconds;
    }

    public HttpURLConnectionFactory getHttpURLConnectionFactory()
    {
        return httpURLConnectionFactory;
    }

    public HttpAgendaProgressReporter setHttpURLConnectionFactory(HttpURLConnectionFactory httpURLConnectionFactory)
    {
        logger.debug("Setting connection factory to " + httpURLConnectionFactory.getClass());
        this.httpURLConnectionFactory = httpURLConnectionFactory;
        return this;
    }

    public URLRequestPerformer getUrlRequestPerformer()
    {
        return urlRequestPerformer;
    }

    public HttpAgendaProgressReporter setUrlRequestPerformer(URLRequestPerformer urlRequestPerformer)
    {
        logger.debug("Setting request performer to " + urlRequestPerformer.getClass());
        this.urlRequestPerformer = urlRequestPerformer;
        return this;
    }

    public String getReportingUrl()
    {
        return reportingUrl;
    }

    public HttpAgendaProgressReporter setReportingUrl(String reportingUrl)
    {
        logger.debug("Setting reporting url to " + reportingUrl);
        this.reportingUrl = reportingUrl;
        return this;
    }

    public String getProxyHost()
    {
        return proxyHost;
    }

    public HttpAgendaProgressReporter setProxyHost(String proxy)
    {
        this.proxyHost = proxy;
        return this;
    }

    public String getProxyPort()
    {
        return proxyPort;
    }

    public HttpAgendaProgressReporter setProxyPort(String proxy)
    {
        this.proxyPort = proxy;
        return this;
    }

    protected void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
