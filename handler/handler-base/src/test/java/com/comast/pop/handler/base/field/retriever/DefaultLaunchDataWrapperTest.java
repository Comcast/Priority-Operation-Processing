package com.comast.pop.handler.base.field.retriever;

import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.OperationProgress;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.api.args.HandlerArgument;
import com.comast.pop.handler.base.field.retriever.environment.EnvironmentFieldRetriever;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelperException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultLaunchDataWrapperTest
{
    private static final JsonHelper jsonHelper = new JsonHelper();
    private static final String JOB_PARAM_NAME = "jobInfo";
    private static final String JOB_ID = "123456-987654";
    private static final String OPERATION_PROGRESS_JSON = "{\"params\": {\"" + JOB_PARAM_NAME +"\": " + jsonHelper.getJSONString(new LastProgressObject(JOB_ID)) + " } }";

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
        doReturn(OPERATION_PROGRESS_JSON).when(mockEnvironmentFieldRetriever).getField(HandlerField.LAST_PROGRESS.name());
        // use a real one for testing string translate
        launchDataWrapper.setJsonHelper(jsonHelper);
        OperationProgress lastOperationProgress = launchDataWrapper.getLastOperationProgress();
        Assert.assertNotNull(lastOperationProgress);
        Assert.assertNotNull(lastOperationProgress.getParams());
        Assert.assertTrue(lastOperationProgress.getParams().containsKey(JOB_PARAM_NAME));
    }

    @Test
    public void testGetLastOperationProgressJsonError()
    {
        doReturn(OPERATION_PROGRESS_JSON).when(mockEnvironmentFieldRetriever).getField(HandlerField.LAST_PROGRESS.name());
        doThrow(new JsonHelperException("")).when(mockJsonHelper).getObjectFromString(anyString(), any());
        OperationProgress lastOperationProgress = launchDataWrapper.getLastOperationProgress();
        verify(mockJsonHelper, times(1)).getObjectFromString(anyString(), any());
        Assert.assertNull(lastOperationProgress);
    }

    @Test
    public void testGetLastOperationProgressParam()
    {
        doReturn(OPERATION_PROGRESS_JSON).when(mockEnvironmentFieldRetriever).getField(HandlerField.LAST_PROGRESS.name());
        // use a real one for testing string translate
        launchDataWrapper.setJsonHelper(jsonHelper);
        LastProgressObject lastProgressObject = launchDataWrapper.getLastOperationProgressParam(JOB_PARAM_NAME, LastProgressObject.class);
        Assert.assertNotNull(lastProgressObject);
        Assert.assertEquals(lastProgressObject.getJobId(), JOB_ID);
    }

    @DataProvider
    public Object[][] lastOperationProgressParamMissingProvider()
    {
        return new Object[][]
            {
                {null},
                {new OperationProgress()},
                {createOperationProgress(null)}
            };
    }

    @Test(dataProvider = "lastOperationProgressParamMissingProvider")
    public void testGetLastOperationProgressParamMissing(OperationProgress operationProgress)
    {
        if(operationProgress != null)
            doReturn(jsonHelper.getJSONString(operationProgress)).when(mockEnvironmentFieldRetriever).getField(HandlerField.LAST_PROGRESS.name());
        Assert.assertNull(launchDataWrapper.getLastOperationProgressParam(JOB_PARAM_NAME, LastProgressObject.class));
    }

    private OperationProgress createOperationProgress(ParamsMap paramsMap)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setParams(paramsMap);
        return operationProgress;
    }

    public static class LastProgressObject
    {
        // this is intentionally different than the one in the test
        public static final String JOB_ID = UUID.randomUUID().toString();
        private String jobId = JOB_ID;

        public LastProgressObject()
        {
        }

        public LastProgressObject(String jobId)
        {
            this.jobId = jobId;
        }

        public String getJobId()
        {
            return jobId;
        }

        public void setJobId(String jobId)
        {
            this.jobId = jobId;
        }
    }
}
