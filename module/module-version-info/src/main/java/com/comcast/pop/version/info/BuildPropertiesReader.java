package com.comcast.pop.version.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.PropertyResourceBundle;

/**
 * BuildProperties wrapper for reading certain sets of BuildProperties objects
 */
public class BuildPropertiesReader
{
    private static final Logger logger = LoggerFactory.getLogger(BuildPropertiesReader.class);

    private final String buildPropertiesFile;

    private ResourceRetriever resourcesRetriever = new ResourceRetriever();
    private List<BuildProperties> allBuildProperties;

    public BuildPropertiesReader(String buildPropertiesFile)
    {
        this.buildPropertiesFile = buildPropertiesFile;
    }

    /**
     * Gets the first build BuildProperties tagged as a service
     * @return The BuildProperties or null
     */
    public BuildProperties getFirstServiceBuildProperties()
    {
        readAllBuildProperties();
        for(BuildProperties buildProperties : allBuildProperties)
        {
            if(buildProperties.isService())
            {
                return buildProperties;
            }
        }
        return null;
    }

    /**
     * Gets all the non-service tagged BuildProperties
     * @return List of all non-service BuildProperties
     */
    public List<BuildProperties> getNonServiceBuildProperties()
    {
        readAllBuildProperties();
        List<BuildProperties> nonServiceProperties = new LinkedList<>();
        for(BuildProperties buildProperties : allBuildProperties)
        {
            if(!buildProperties.isService())
            {
                nonServiceProperties.add(buildProperties);
            }
        }
        return nonServiceProperties;
    }

    /**
     * Gets all the BuildProperties
     * @return full list of BuildProperties
     */
    public List<BuildProperties> getAllBuildProperties()
    {
        readAllBuildProperties();
        return allBuildProperties;
    }

    protected synchronized void readAllBuildProperties()
    {
        if(allBuildProperties == null)
        {
            allBuildProperties = new LinkedList<>();
            try
            {
                List<InputStream> inputStreams =
                        resourcesRetriever.getResources(this.getClass().getClassLoader(), buildPropertiesFile);
                for (InputStream inputStream : inputStreams)
                {
                    try
                    {
                        allBuildProperties.add(new BuildProperties(
                                new PropertyResourceBundle(inputStream)));
                        // take no chances...
                        inputStream.close();
                    }
                    catch(Throwable t)
                    {
                        logger.error("Failed to load property from stream.", t);
                        // ignore problematic properties, but continue
                    }
                }
            }
            catch(Throwable t)
            {
                logger.error(String.format("Failed to load resources from %1$s", buildPropertiesFile), t);
                // allBuildProperties will be empty if resource loading goes awry
            }
        }
    }

    public void setResourceRetriever(ResourceRetriever resourcesRetriever)
    {
        this.resourcesRetriever = resourcesRetriever;
    }
}
