package com.theplatform.dfh.version;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Basic wrapper around ClassLoader.getResources
 */
public class ResourceRetriever
{
    public ResourceRetriever()
    {

    }

    /**
     * Gets all the resources by the given resource name and returns the streams to them
     * @param classloader The class loader to retrieve the resources with
     * @param resourceName The resource to retrieve
     * @return List of InputStreams for each resource
     * @throws IOException See ClassLoader.getResources or URL.openStream
     */
    public List<InputStream> getResources(ClassLoader classloader, String resourceName) throws IOException
    {
        List<InputStream> inputStreams = new LinkedList<>();

        Enumeration<URL> resourceEnum = classloader.getResources(resourceName);

        while(resourceEnum.hasMoreElements())
        {
            inputStreams.add(resourceEnum.nextElement().openStream());
        }

        return inputStreams;
    }
}
