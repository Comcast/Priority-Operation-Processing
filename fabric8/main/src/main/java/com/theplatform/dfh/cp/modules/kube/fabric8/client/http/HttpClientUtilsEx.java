package com.theplatform.dfh.cp.modules.kube.fabric8.client.http;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.factory.SSLSocketFactoryWrapper;
import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.ConnectionTracker;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.internal.SSLUtils;
import io.fabric8.kubernetes.client.utils.ImpersonatorInterceptor;
import okhttp3.Authenticator;
import okhttp3.ConnectionSpec;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static io.fabric8.kubernetes.client.utils.Utils.isNotNullOrEmpty;
import static okhttp3.ConnectionSpec.CLEARTEXT;

/**
 * This is a near duplicate of the HttpClientUtils in fabric8 3.1.7. - with added support for the ConnectionTracker
 */
public class HttpClientUtilsEx
{
    public static OkHttpClient createHttpClient(final Config config, final ConnectionTracker connectionTracker) {
        try {
            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

            // Follow any redirects
            httpClientBuilder.followRedirects(true);
            httpClientBuilder.followSslRedirects(true);

            if (config.isTrustCerts()) {
                httpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });
            }

            TrustManager[] trustManagers = SSLUtils.trustManagers(config);
            KeyManager[] keyManagers = SSLUtils.keyManagers(config);

            if (keyManagers != null || trustManagers != null || config.isTrustCerts()) {
                X509TrustManager trustManager = null;
                if (trustManagers != null && trustManagers.length == 1) {
                    trustManager = (X509TrustManager) trustManagers[0];
                }

                try {
                    SSLContext sslContext = SSLUtils.sslContext(keyManagers, trustManagers, config.isTrustCerts());
                    SSLSocketFactory sslSocketFactory = new SSLSocketFactoryWrapper(sslContext.getSocketFactory(), connectionTracker);
                    httpClientBuilder.sslSocketFactory(sslSocketFactory, trustManager);
                } catch (GeneralSecurityException e) {
                    throw new AssertionError(); // The system has no TLS. Just give up.
                }
            } else {
                SSLContext context = SSLContext.getInstance("TLSv1.2");
                context.init(keyManagers, trustManagers, null);
                SSLSocketFactory sslSocketFactory = new SSLSocketFactoryWrapper(context.getSocketFactory(), connectionTracker);
                httpClientBuilder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0]);
            }

            httpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException
                {
                    Request request = chain.request();
                    if (isNotNullOrEmpty(config.getUsername()) && isNotNullOrEmpty(config.getPassword())) {
                        Request authReq = chain.request().newBuilder().addHeader("Authorization", Credentials.basic(config.getUsername(), config.getPassword())).build();
                        return chain.proceed(authReq);
                    } else if (isNotNullOrEmpty(config.getOauthToken())) {
                        Request authReq = chain.request().newBuilder().addHeader("Authorization", "Bearer " + config.getOauthToken()).build();
                        return chain.proceed(authReq);
                    }
                    return chain.proceed(request);
                }
            }).addInterceptor(new ImpersonatorInterceptor(config.getRequestConfig()));

            Logger reqLogger = LoggerFactory.getLogger(HttpLoggingInterceptor.class);
            if (reqLogger.isTraceEnabled()) {
                HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                httpClientBuilder.addNetworkInterceptor(loggingInterceptor);
            }

            if (config.getConnectionTimeout() > 0) {
                httpClientBuilder.connectTimeout(config.getConnectionTimeout(), TimeUnit.MILLISECONDS);
            }

            if (config.getRequestTimeout() > 0) {
                httpClientBuilder.readTimeout(config.getRequestTimeout(), TimeUnit.MILLISECONDS);
            }

            if (config.getWebsocketPingInterval() > 0) {
                httpClientBuilder.pingInterval(config.getWebsocketPingInterval(), TimeUnit.MILLISECONDS);
            }

            if (config.getMaxConcurrentRequestsPerHost() > 0) {
                Dispatcher dispatcher = new Dispatcher();
                dispatcher.setMaxRequests(config.getMaxConcurrentRequests());
                dispatcher.setMaxRequestsPerHost(config.getMaxConcurrentRequestsPerHost());
                httpClientBuilder.dispatcher(dispatcher);
            }

            // Only check proxy if it's a full URL with protocol
            if (config.getMasterUrl().toLowerCase().startsWith(Config.HTTP_PROTOCOL_PREFIX) || config.getMasterUrl().startsWith(Config.HTTPS_PROTOCOL_PREFIX)) {
                try {
                    URL proxyUrl = getProxyUrl(config);
                    if (proxyUrl != null) {
                        httpClientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyUrl.getHost(), proxyUrl.getPort())));

                        if (config.getProxyUsername() != null) {
                            httpClientBuilder.proxyAuthenticator(new Authenticator() {
                                @Override
                                public Request authenticate(Route route, Response response) throws IOException {

                                    String credential = Credentials.basic(config.getProxyUsername(), config.getProxyPassword());
                                    return response.request().newBuilder().header("Proxy-Authorization", credential).build();
                                }
                            });
                        }
                    }

                } catch (MalformedURLException e) {
                    throw new KubernetesClientException("Invalid proxy server configuration", e);
                }
            }

            if (config.getUserAgent() != null && !config.getUserAgent().isEmpty()) {
                httpClientBuilder.addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request agent = chain.request().newBuilder().header("User-Agent", config.getUserAgent()).build();
                        return chain.proceed(agent);
                    }
                });
            }

            if (config.getTlsVersions() != null && config.getTlsVersions().length > 0) {
                ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(config.getTlsVersions())
                    .build();
                httpClientBuilder.connectionSpecs(Arrays.asList(spec, CLEARTEXT));
            }

            return httpClientBuilder.build();
        } catch (Exception e) {
            throw KubernetesClientException.launderThrowable(e);
        }
    }

    private static URL getProxyUrl(Config config) throws MalformedURLException {
        URL master = new URL(config.getMasterUrl());
        String host = master.getHost();
        if (config.getNoProxy() != null) {
            for (String noProxy : config.getNoProxy()) {
                if (host.endsWith(noProxy)) {
                    return null;
                }
            }
        }
        String proxy = config.getHttpsProxy();
        if (master.getProtocol().equals("http")) {
            proxy = config.getHttpProxy();
        }
        if (proxy != null) {
            return new URL(proxy);
        }
        return null;
    }
}
