package com.comcast.pop.version.info;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class BuildPropertiesReaderTest
{
    private final String NEWLINE = System.getProperty("line.separator");
    private BuildPropertiesReader buildPropertiesReader;

    private ResourceRetriever mockResourceRetriever;

    @BeforeMethod
    public void setup() throws Exception
    {
        buildPropertiesReader =
                new BuildPropertiesReader(ServiceBuildPropertiesContainer.BUILD_PROPERTIES_FILE);
        mockResourceRetriever = mock(ResourceRetriever.class);
        buildPropertiesReader.setResourceRetriever(mockResourceRetriever);
    }

    @DataProvider
    public Object[][] ServicePropertiesCountProvider()
    {
        return new Object[][]
                {
                        {0},
                        {1},
                        {5}
                };
    }

    @Test(dataProvider = "ServicePropertiesCountProvider")
    public void testGetFirstServiceBuildProperties(final int SERVICE_PROPERTIES_COUNT) throws Exception
    {
        final int NON_SERVICE_PROPERTIES_COUNT = 10;

        generateResourceContents(SERVICE_PROPERTIES_COUNT, NON_SERVICE_PROPERTIES_COUNT);
        BuildProperties result = buildPropertiesReader.getFirstServiceBuildProperties();

        if(SERVICE_PROPERTIES_COUNT > 0)
        {
            Assert.assertNotNull(result);
            assertAllPropertiesValid(Collections.singletonList(result));
        }
        else
        {
            Assert.assertNull(result);
        }
    }

    @Test
    public void testBuildPropertyInvalid() throws Exception
    {
        // this is just one example...
        List<InputStream> testResourceStreams = new LinkedList<>();
        testResourceStreams.add(
                new ByteArrayInputStream(getKeyValue(BuildProperties.BUILD_NAME_PROP, "sample").getBytes()));

        doReturn(testResourceStreams).when(mockResourceRetriever).getResources(any(), anyString());

        List<BuildProperties> result = buildPropertiesReader.getAllBuildProperties();
        Assert.assertEquals(result.size(), 1);
        Assert.assertFalse(result.get(0).isValid());
    }

    @Test
    public void testNullBuildPropertyStream() throws Exception
    {
        // this is just one example...
        List<InputStream> testResourceStreams = new LinkedList<>();
        testResourceStreams.add(null);

        doReturn(testResourceStreams).when(mockResourceRetriever).getResources(any(), anyString());

        List<BuildProperties> result = buildPropertiesReader.getAllBuildProperties();
        Assert.assertEquals(result.size(), 0);
    }

    @Test(dataProvider = "ServicePropertiesCountProvider")
    public void getNonServiceBuildProperties(final int SERVICE_PROPERTIES_COUNT) throws Exception
    {
        final int NON_SERVICE_PROPERTIES_COUNT = 10;

        generateResourceContents(SERVICE_PROPERTIES_COUNT, NON_SERVICE_PROPERTIES_COUNT);
        List<BuildProperties> result = buildPropertiesReader.getNonServiceBuildProperties();
        Assert.assertEquals(result.size(), NON_SERVICE_PROPERTIES_COUNT);
        assertAllPropertiesValid(result);
    }

    @DataProvider
    public Object[][] PropertiesCountProvider()
    {
        return new Object[][]
                {
                        {0, 0},
                        {1, 0},
                        {0, 1},
                        {10, 10}
                };
    }

    @Test(dataProvider = "PropertiesCountProvider")
    public void testGetAllBuildProperties(final int SERVICE_PROPERTIES_COUNT, final int NON_SERVICE_PROPERTIES_COUNT) throws Exception
    {
        generateResourceContents(SERVICE_PROPERTIES_COUNT, NON_SERVICE_PROPERTIES_COUNT);
        List<BuildProperties> result = buildPropertiesReader.getAllBuildProperties();
        Assert.assertEquals(result.size(), SERVICE_PROPERTIES_COUNT + NON_SERVICE_PROPERTIES_COUNT);
        assertAllPropertiesValid(result);
    }

    @Test
    public void throwExceptionOnRetrieve() throws Exception
    {
        doThrow(new RuntimeException()).when(mockResourceRetriever).getResources(any(), anyString());
        buildPropertiesReader.getAllBuildProperties();
        // no exception should be thrown
    }

    protected void assertAllPropertiesValid(List<BuildProperties> buildProperties)
    {
        for(BuildProperties props : buildProperties)
        {
            Assert.assertTrue(props.isValid());
        }
    }

    protected void generateResourceContents(int serviceCount, int nonServiceCount) throws IOException
    {
        List<InputStream> testResourceStreams = new LinkedList<>();
        for(int x = 0; x < serviceCount; x++)
        {
            testResourceStreams.add(
                    new ByteArrayInputStream(getBuildPropertiesAsString(x + "", true).getBytes()));
        }
        for(int x = 0; x < nonServiceCount; x++)
        {
            testResourceStreams.add(
                    new ByteArrayInputStream(getBuildPropertiesAsString(x + "", false).getBytes()));
        }
        doReturn(testResourceStreams).when(mockResourceRetriever).getResources(any(), anyString());
    }

    protected String getBuildPropertiesAsString(String name, boolean isService)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getKeyValue(BuildProperties.BUILD_NAME_PROP, name));
        stringBuilder.append(getKeyValue(BuildProperties.BUILD_TITLE_PROP, name));
        stringBuilder.append(getKeyValue(BuildProperties.BUILD_LABEL_PROP, name));
        stringBuilder.append(getKeyValue(BuildProperties.IS_SERVICE_PROP, isService + ""));
        stringBuilder.append(getKeyValue(BuildProperties.JAR_NAME_PROP, name));
        stringBuilder.append(getKeyValue(BuildProperties.BUILD_DATE_PROP, name));
        stringBuilder.append(getKeyValue(BuildProperties.VERSION_PROP, name));
        return stringBuilder.toString();
    }

    protected String getKeyValue(String key, String value)
    {
        return String.format("%1$s=%2$s%3$s", key, value, NEWLINE);
    }
}
