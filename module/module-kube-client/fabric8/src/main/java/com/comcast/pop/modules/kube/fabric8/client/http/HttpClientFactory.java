package com.comcast.pop.modules.kube.fabric8.client.http;

import com.comcast.pop.modules.kube.fabric8.client.factory.SSLSocketFactoryWrapper;
import com.comcast.pop.modules.kube.fabric8.client.watcher.ConnectionTracker;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.internal.SSLUtils;
import io.fabric8.kubernetes.client.utils.HttpClientUtils;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.GeneralSecurityException;

/**
 * Factory for creating custom OkHttpClient objects with a shared ConnectionPool and a connection tracker. This is a wrapper for HttpClientUtils.createHttpClient
 *
 * This is primarily to combat the endless creation of ConnectionPool objects as well as issues with Socket close calls with infinite read timeouts
 */
public class HttpClientFactory
{
    private static ConnectionPool sharedConnectionPool = new ConnectionPool();

    public OkHttpClient createHttpClient(final Config config, final ConnectionTracker connectionTracker)
    {
        return createHttpClient(config, connectionTracker, true);
    }

    public OkHttpClient createHttpClient(final Config config, final ConnectionTracker connectionTracker, boolean useSharedConnectionPool)
    {
        // HACK explanation: We use the HttpClientUtils.createHttpClient to create a client and then generate a builder from that client so we can
        // set the connectionPool and wrap the SSLSocketFactory
        OkHttpClient client = HttpClientUtils.createHttpClient(config);
        OkHttpClient.Builder httpClientBuilder = client.newBuilder();
        if(useSharedConnectionPool)
            httpClientBuilder.connectionPool(sharedConnectionPool);
        // NOTE: DO NOT use the deprecated httpClientBuilder.sslSocketFactory(SSLSocketFactory) - it just results in exceptions (and is deprecated anyway!)
        // sadly the sslSocketFactory method assigns a field we cannot access and reference easily (certificateChainCleaner and the associated trustManager)
        configureBuilderSSLSocketFactory(config, connectionTracker, httpClientBuilder);
        return httpClientBuilder.build();
    }

    /**
     * Configures the SSLFactory and trustmanager with a wrapped SSLSocketFactory to track socket creation
     * This is necessary because the trustManager must be determined/passed into the setter (OkHttpClient.Builder.sslSocketFactory)
     *
     * THIS IS A NEAR COPY OF THE TRUST MANAGER SETUP IN HttpClientUtils.createHttpClient
     *
     * @param config Config to get the certs from
     * @param connectionTracker The ConnectionTracker to track socket creates
     * @param httpClientBuilder The builder to set the SSLFactory on
     * @return The builder with the SSLFactory setup based on the settings
     */
    protected static OkHttpClient.Builder configureBuilderSSLSocketFactory(final Config config, final ConnectionTracker connectionTracker, OkHttpClient.Builder httpClientBuilder)
    {
        try
        {
            TrustManager[] trustManagers = SSLUtils.trustManagers(config);
            KeyManager[] keyManagers = SSLUtils.keyManagers(config);

            if (keyManagers != null || trustManagers != null || config.isTrustCerts())
            {
                X509TrustManager trustManager = null;
                if (trustManagers != null && trustManagers.length == 1)
                {
                    trustManager = (X509TrustManager) trustManagers[0];
                }

                try
                {
// START KUBE-CHANGES - use the SSLSocketFactoryWrapper
                    SSLContext sslContext = SSLUtils.sslContext(keyManagers, trustManagers, config.isTrustCerts());
                    SSLSocketFactory sslSocketFactory = new SSLSocketFactoryWrapper(sslContext.getSocketFactory(), connectionTracker);
                    httpClientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
// END KUBE-CHANGES - use the SSLSocketFactoryWrapper
                }
                catch (GeneralSecurityException e)
                {
                    throw new AssertionError(); // The system has no TLS. Just give up.
                }
            }
            else
            {
                SSLContext context = SSLContext.getInstance("TLSv1.2");
                context.init(keyManagers, trustManagers, null);
// START KUBE-CHANGES - use the SSLSocketFactoryWrapper
                SSLSocketFactory sslSocketFactory = new SSLSocketFactoryWrapper(context.getSocketFactory(), connectionTracker);
                httpClientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0]);
// END KUBE-CHANGES - use the SSLSocketFactoryWrapper
            }

            return httpClientBuilder;
        }
        catch (Exception e) {
            throw KubernetesClientException.launderThrowable(e);
        }
    }
}
