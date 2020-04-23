package com.comcast.pop.version.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.PropertyResourceBundle;

/**
 * Build properties based heavily on BuildBean from pl-management-status (a somewhat heavy weight dependency)
 */
public class BuildProperties
{
    private static final Logger logger = LoggerFactory.getLogger(BuildProperties.class);

    public static final String BUILD_NAME_PROP = "build.name";
    public static final String BUILD_TITLE_PROP = "build.title";
    public static final String BUILD_LABEL_PROP = "build.label";
    public static final String IS_SERVICE_PROP = "service";
    public static final String JAR_NAME_PROP = "jar.name";
    public static final String BUILD_DATE_PROP = "build.date";
    public static final String VERSION_PROP = "version";

    private static final String DEFAULT_VERISON = "Unknown";
    private static final String DEFAULT_BUILD_LABEL =
            "Project/module build file has not been set up to enable build label information.";

    private String buildName;
    private String buildTitle;
    private boolean service;
    private String jarName;
    private String buildDate;
    private String version = DEFAULT_VERISON;
    private String buildLabel = DEFAULT_BUILD_LABEL;
    private boolean valid;

    private PropertyResourceBundle propertyResourceBundle;

    public BuildProperties()
    {
        valid = false;
    }

    /**
     * Creates a BuildProperties based on the passed in PropertyResourceBundle
     * @param propertyResourceBundle The bundle to extract the properties from
     */
    public BuildProperties(PropertyResourceBundle propertyResourceBundle)
    {
        this.propertyResourceBundle = propertyResourceBundle;
        valid = true;

        buildName = getStringProperty(BUILD_NAME_PROP, null);
        buildTitle = getStringProperty(BUILD_TITLE_PROP, null);
        service = getBooleanProperty(IS_SERVICE_PROP, Boolean.FALSE.toString());
        buildDate = getStringProperty(BUILD_DATE_PROP, null);
        jarName = getStringProperty(JAR_NAME_PROP, null);
        buildLabel = getStringProperty(BUILD_LABEL_PROP, DEFAULT_BUILD_LABEL);
        version = getStringProperty(VERSION_PROP, DEFAULT_VERISON);
    }

    private boolean getBooleanProperty(String propertyName, String defaultValue)
    {
        return Boolean.valueOf(getStringProperty(propertyName, defaultValue));
    }

    private String getStringProperty(String propertyName, String defaultValue)
    {
        try
        {
            return propertyResourceBundle.getString(propertyName);
        }
        catch(Throwable t)
        {
            logger.error(String.format("Failed to read property: %1$s", propertyName), t);
            valid = false;
        }
        return defaultValue;
    }

    public String getBuildName()
    {
        return buildName;
    }

    public BuildProperties setBuildName(String buildName)
    {
        this.buildName = buildName;
        return this;
    }

    public String getBuildTitle()
    {
        return buildTitle;
    }

    public BuildProperties setBuildTitle(String buildTitle)
    {
        this.buildTitle = buildTitle;
        return this;
    }

    public boolean isService()
    {
        return service;
    }

    public BuildProperties setService(boolean service)
    {
        this.service = service;
        return this;
    }

    public String getJarName()
    {
        return jarName;
    }

    public BuildProperties setJarName(String jarName)
    {
        this.jarName = jarName;
        return this;
    }

    public String getBuildDate()
    {
        return buildDate;
    }

    public BuildProperties setBuildDate(String buildDate)
    {
        this.buildDate = buildDate;
        return this;
    }

    public boolean isValid()
    {
        return valid;
    }

    protected BuildProperties setValid(boolean valid)
    {
        this.valid = valid;
        return this;
    }

    public String getVersion()
    {
        return version;
    }

    public BuildProperties setVersion(String version)
    {
        this.version = version;
        return this;
    }

    public String getBuildLabel()
    {
        return buildLabel;
    }

    public BuildProperties setBuildLabel(String buildLabel)
    {
        this.buildLabel = buildLabel;
        return this;
    }

    @Override
    public String toString()
    {
        return "BuildProperties{" +
                "buildName='" + buildName + '\'' +
                ", buildTitle='" + buildTitle + '\'' +
                ", service=" + service +
                ", jarName='" + jarName + '\'' +
                ", buildDate='" + buildDate + '\'' +
                ", version='" + version + '\'' +
                ", buildLabel='" + buildLabel + '\'' +
                ", valid=" + valid +
                '}';
    }
}
