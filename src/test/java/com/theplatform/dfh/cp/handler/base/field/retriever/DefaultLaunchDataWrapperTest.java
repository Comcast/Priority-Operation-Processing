package com.theplatform.dfh.cp.handler.base.field.retriever;

import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.field.api.args.HandlerArgument;
import com.theplatform.dfh.cp.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultLaunchDataWrapperTest
{
    final String PAYLOAD_VALUE = "{}";
    private static final String TEST_FILE = "./src/test/resources/test.json";
    private DefaultLaunchDataWrapper launchDataWrapper;
    private JsonHelper mockJsonHelper;
    private EnvironmentFieldRetriever mockEnvironmentFieldRetriever;

    @BeforeMethod
    public void setup()
    {
        mockJsonHelper = mock(JsonHelper.class);
        mockEnvironmentFieldRetriever = mock(EnvironmentFieldRetriever.class);
        launchDataWrapper = new DefaultLaunchDataWrapper(new String[]{});
        launchDataWrapper.setEnvironmentRetriever(mockEnvironmentFieldRetriever);
        launchDataWrapper.setJsonHelper(mockJsonHelper);
    }

    // this test may run into pathing issues due to where the test is started (it is also more an integration test and files are only used for local dev generally)
    @Test(enabled = false)
    public void testGetStringFromFieldsFile()
    {
        //System.out.println(System.getProperty("user.dir"));
        launchDataWrapper = new DefaultLaunchDataWrapper(new String[] { "-" + HandlerArgument.PAYLOAD_FILE.getArgumentName(), TEST_FILE});
        String data = launchDataWrapper.getStringFromFields(HandlerArgument.PAYLOAD_FILE, HandlerField.PAYLOAD);
        Assert.assertEquals(data, PAYLOAD_VALUE);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Failed to load data from.*")
    public void testGetStringFromFieldsFileError()
    {
        final String BAD_FILE = TEST_FILE + "x";
        launchDataWrapper = new DefaultLaunchDataWrapper(new String[] { "-" + HandlerArgument.PAYLOAD_FILE.getArgumentName(), BAD_FILE});
        launchDataWrapper.getStringFromFields(HandlerArgument.PAYLOAD_FILE, HandlerField.PAYLOAD);
    }

    @Test
    public void testGetStringFromFieldsArg()
    {
        doReturn(PAYLOAD_VALUE).when(mockEnvironmentFieldRetriever).getField(HandlerField.PAYLOAD.name());
        String data = launchDataWrapper.getStringFromFields(HandlerArgument.PAYLOAD_FILE, HandlerField.PAYLOAD);
        verify(mockEnvironmentFieldRetriever, times(1)).getField(anyString());
        Assert.assertEquals(data, PAYLOAD_VALUE);
    }

    @Test
    public void testGetStringFromFieldsArgMissing()
    {
        String data = launchDataWrapper.getStringFromFields(HandlerArgument.PAYLOAD_FILE, HandlerField.PAYLOAD);
        verify(mockEnvironmentFieldRetriever, times(1)).getField(anyString());
        Assert.assertNull(data);
    }

    @Test
    public void testGetLastOperationProgress()
    {

    }

    @Test
    public void testGetLastOperationProgressJsonError()
    {
        doThrow(new JsonHelperException("")).when(mockJsonHelper).getObjectFromString(anyString(), any());
    }
}
