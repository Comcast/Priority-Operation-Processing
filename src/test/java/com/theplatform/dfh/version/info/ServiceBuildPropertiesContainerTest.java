package com.theplatform.dfh.version.info;

import org.slf4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class ServiceBuildPropertiesContainerTest
{
    private BuildPropertiesReader mockBuildPropertiesReader;
    private Logger mockLogger;

    @BeforeMethod
    public void setup()
    {
        ServiceBuildPropertiesContainer.resetBuildProperties();
        mockBuildPropertiesReader = mock(BuildPropertiesReader.class);
        ServiceBuildPropertiesContainer.setBuildPropertyReader(mockBuildPropertiesReader);
        mockLogger = mock(Logger.class);
    }

    @Test
    public void testLogServiceBuildString()
    {
        doReturn(new BuildProperties().setValid(true)).when(mockBuildPropertiesReader).getFirstServiceBuildProperties();
        ServiceBuildPropertiesContainer.logServiceBuildString(mockLogger);
        verify(mockLogger, times(1)).info(anyString());
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testLogServiceBuildStringInvalid()
    {
        doReturn(null).when(mockBuildPropertiesReader).getFirstServiceBuildProperties();
        ServiceBuildPropertiesContainer.logServiceBuildString(mockLogger);
    }

    @Test
    public void testLogServiceBuildStringInvalidAllowed()
    {
        doReturn(null).when(mockBuildPropertiesReader).getFirstServiceBuildProperties();
        ServiceBuildPropertiesContainer.logServiceBuildString(mockLogger, false);
    }

    @Test
    public void testGetBuildProperties()
    {
        BuildProperties validProperties = new BuildProperties().setValid(true);
        doReturn(validProperties).when(mockBuildPropertiesReader).getFirstServiceBuildProperties();
        BuildProperties result = ServiceBuildPropertiesContainer.getBuildProperties();
        Assert.assertEquals(result, validProperties);
        BuildProperties resultCached = ServiceBuildPropertiesContainer.getBuildProperties();
        Assert.assertEquals(resultCached, validProperties);
    }
}
