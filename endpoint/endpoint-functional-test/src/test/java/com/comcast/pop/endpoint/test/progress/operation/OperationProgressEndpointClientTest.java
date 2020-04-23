package com.comcast.pop.endpoint.test.progress.operation;

import com.comcast.pop.endpoint.test.BaseEndpointObjectClientTest;
import com.comcast.pop.endpoint.test.factory.DataGenerator;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.api.progress.DiagnosticEvent;
import com.comcast.pop.api.progress.OperationProgress;
import com.comcast.pop.api.progress.ProcessingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 */
public class OperationProgressEndpointClientTest extends BaseEndpointObjectClientTest<OperationProgress>
{
    private static final Logger logger = LoggerFactory.getLogger(OperationProgressEndpointClientTest.class);
    private final String EXTERNAL_ID = "theExternalId";
    private final ProcessingState UPDATED_PROCESSING_STATE = ProcessingState.WAITING;
    private final String UPDATED_STATE_MESSAGE = "ExampleStateMessage";
    private final Date UPDATED_START_TIME = new GregorianCalendar(1998, 1, 15, 12, 30).getTime();
    private final String DIAGNOSTIC_MESSAGE = "theDiagnostic";
    private final String DIAGNOSTIC_MESSAGE_UPDATE = "theDiagnosticUpdate";

    public OperationProgressEndpointClientTest()
    {
        super(OperationProgress.class);
    }

    @Override
    public String getEndpointUrl()
    {
        return operationProgressUrl;
    }

    @Override
    protected OperationProgress getTestObject()
    {
        OperationProgress progress = DataGenerator.getOperationProgress(testCustomerId, UUID.randomUUID().toString());
        ParamsMap paramsMap = new ParamsMap();
        paramsMap.put(GeneralParamKey.externalId, EXTERNAL_ID);
        progress.setParams(paramsMap);
        progress.setDiagnosticEvents(new DiagnosticEvent[]{new DiagnosticEvent(DIAGNOSTIC_MESSAGE)});
        return progress;
    }

    @Override
    protected OperationProgress updateTestObject(OperationProgress object)
    {
        OperationProgress sparseProgress = new OperationProgress();
        sparseProgress.setId(object.getId());
        sparseProgress.setProcessingState(UPDATED_PROCESSING_STATE);
        sparseProgress.setProcessingStateMessage(UPDATED_STATE_MESSAGE);
        sparseProgress.setStartedTime(UPDATED_START_TIME);
        sparseProgress.setDiagnosticEvents(new DiagnosticEvent[]{new DiagnosticEvent(DIAGNOSTIC_MESSAGE), new DiagnosticEvent(DIAGNOSTIC_MESSAGE_UPDATE)});
        return sparseProgress;
    }

    @Override
    protected void verifyCreatedTestObject(OperationProgress created, OperationProgress testObject)
    {
        Assert.assertEquals(created.getAgendaProgressId(), testObject.getAgendaProgressId());
        Assert.assertNotNull(created.getParams());
        Assert.assertEquals(created.getParams().get(GeneralParamKey.externalId), EXTERNAL_ID);
        Assert.assertNotNull(created.getDiagnosticEvents());
        Assert.assertEquals(created.getDiagnosticEvents().length, 1);
        Assert.assertEquals(created.getDiagnosticEvents()[0].getMessage(), DIAGNOSTIC_MESSAGE);
    }

    @Override
    protected void verifyUpdatedTestObject(OperationProgress object)
    {
//        Assert.assertEquals(object.getAgendaProgressId(), UPDATED_PROGRESS_ID);
        Assert.assertEquals(object.getProcessingState(), UPDATED_PROCESSING_STATE);
        Assert.assertEquals(object.getProcessingStateMessage(), UPDATED_STATE_MESSAGE);
        Assert.assertEquals(object.getStartedTime(), UPDATED_START_TIME);
        Assert.assertNotNull(object.getParams());
        Assert.assertEquals(object.getParams().get(GeneralParamKey.externalId), EXTERNAL_ID);
        Assert.assertNotNull(object.getDiagnosticEvents());
        Assert.assertEquals(object.getDiagnosticEvents().length, 2);
        Assert.assertEquals(object.getDiagnosticEvents()[0].getMessage(), DIAGNOSTIC_MESSAGE);
        Assert.assertEquals(object.getDiagnosticEvents()[1].getMessage(), DIAGNOSTIC_MESSAGE_UPDATE);
    }
}
