package com.theplatform.dfh.cp.modules.kube.fabric8.client.watcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;

/**
 * Tracker for sockets created in association with a given pod. Offers basic utilities related to the sockets.
 */
public class ConnectionTracker
{
    private static Logger logger = LoggerFactory.getLogger(ConnectionTracker.class);

    private List<Socket> sockets = new LinkedList<>();

    private String podName = "unknown";

    public ConnectionTracker setPodName(String podName)
    {
        this.podName = podName;
        return this;
    }

    public synchronized void addSocket(Socket socket)
    {
        sockets.add(socket);
    }

    /**
     * Updates the timeout socket option on all un-closed sockets associated with the tracker
     * @param timeout The timeout to set
     */
    public synchronized void setSocketTimeouts(int timeout)
    {
        sockets.stream()
            .filter(socket -> !socket.isClosed())
            .forEach(socket ->
            {
                try
                {
                    if(!socket.isClosed())
                    {
                        logger.debug("Attempting to update socket timeout {} -> {} for pod: {}", socket.getSoTimeout(), timeout, podName);
                        socket.setSoTimeout(timeout);
                    }
                }
                catch (SocketException e)
                {
                    logger.error(String.format("Failed to update socket timeout to: %1$s for pod: %2$s", timeout, podName), e);
                }
            });
    }
}
