package com.theplatform.dfh.cp.modules.kube.fabric8.client.factory;

import com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher.ConnectionTracker;

import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Wrapper for SSLSocketFactory so we can track all sockets created.
 */
public class SSLSocketFactoryWrapper extends SSLSocketFactory
{
    private final SSLSocketFactory sslSocketFactory;
    private final ConnectionTracker connectionTracker;

    private Socket socketCreated(Socket socket)
    {
        if(connectionTracker != null)
            connectionTracker.addSocket(socket);
        return socket;
    }

    public SSLSocketFactoryWrapper(SSLSocketFactory sslSocketFactory, ConnectionTracker connectionTracker)
    {
        this.sslSocketFactory = sslSocketFactory;
        this.connectionTracker = connectionTracker;
    }

    @Override
    public String[] getDefaultCipherSuites()
    {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites()
    {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException
    {
        return socketCreated(sslSocketFactory.createSocket(socket, s, i, b));
    }

    @Override
    public Socket createSocket(Socket socket, InputStream inputStream, boolean b) throws IOException
    {
        return socketCreated(sslSocketFactory.createSocket(socket, inputStream, b));
    }

    @Override
    public Socket createSocket() throws IOException
    {
        return socketCreated(sslSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException, UnknownHostException
    {
        return socketCreated(sslSocketFactory.createSocket(s, i));
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException
    {
        return socketCreated(sslSocketFactory.createSocket(s, i, inetAddress, i1));
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException
    {
        return socketCreated(sslSocketFactory.createSocket(inetAddress, i));
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException
    {
        return socketCreated(sslSocketFactory.createSocket(inetAddress, i, inetAddress1, i1));
    }


}
