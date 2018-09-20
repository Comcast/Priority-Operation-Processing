package com.theplatform.dfh.version;

import org.slf4j.Logger;

/**
 * Static container for the Service BuildProperties
 */
public class ServiceBuildPropertiesContainer
{
    public final static String BUILD_PROPERTIES_FILE = "plbuild.properties";
    private static BuildProperties buildProperties;
    private static BuildPropertiesReader buildPropertyReader = new BuildPropertiesReader(BUILD_PROPERTIES_FILE);

    /**
     * Logs the service build properties to the specified logger. (will throw a runtime exception if not found)
     * @param logger The logger to log to (info level)
     */
    public static void logServiceBuildString(Logger logger)
    {
        BuildProperties buildProps = getBuildProperties();
        if(buildProps.isValid())
        {
            logger.info(buildProps.toString());
        }
        else
        {
            throw new RuntimeException("No service build properties found! Please configure this in the project pom.xml.");
        }
    }

    protected synchronized static BuildProperties getBuildProperties()
    {
        // only load this once
        if(buildProperties == null)
        {
            buildProperties = buildPropertyReader.getFirstServiceBuildProperties();
            if(buildProperties == null)
            {
                // default the build properties
                buildProperties = new BuildProperties();
            }
        }
        return buildProperties;
    }

    /**
     * Resets the underlying cached BuildProperties to null (primarily for unit testing)
     */
    protected synchronized static void resetBuildProperties()
    {
        buildProperties = null;
    }

    public static void setBuildPropertyReader(BuildPropertiesReader buildPropertyReader)
    {
        ServiceBuildPropertiesContainer.buildPropertyReader = buildPropertyReader;
    }
}
