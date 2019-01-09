package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class TransformRequestProcessorTest
{
    private final String PROGRESS_ID = "theProgressId";
    private final String EXEC_PROGRESS_ID = "theExecProgressId";
    private final String AGENDA_ID = "theAgendaId";
    private final String CUSTOMER_ID = "theCustomer";

    private TransformRequestProcessor transformRequestProcessor;
    private ObjectPersister<TransformRequest> mockTransformRequestPersister;
    private ObjectClient<AgendaProgress> mockAgendaProgressClient;
    private ObjectClient<Agenda> mockAgendaClient;

    @BeforeMethod
    public void setup()
    {
        mockTransformRequestPersister = mock(ObjectPersister.class);
        mockAgendaProgressClient = mock(ObjectClient.class);
        mockAgendaClient = mock(ObjectClient.class);

        transformRequestProcessor = new TransformRequestProcessor(mockTransformRequestPersister, null, null, null, null, null, null);
        transformRequestProcessor.setAgendaClient(mockAgendaClient);
        transformRequestProcessor.setAgendaProgressClient(mockAgendaProgressClient);
    }

    @Test
    public void testHandlePost() throws BadRequestException
    {
        TransformRequest transformRequest = createTransformRequest();
        // NOTE: If the order of the creates changes this will break
        doAnswer(new Answer()
        {
            private Integer callCount = 0;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                AgendaProgress result = new AgendaProgress();
                result.setId(callCount > 0 ? EXEC_PROGRESS_ID : PROGRESS_ID);
                callCount++;
                DataObjectResponse<AgendaProgress> dataObjectResponse = new DefaultDataObjectResponse<>();
                dataObjectResponse.add(result);
                return dataObjectResponse;
            }
        }).when(mockAgendaProgressClient).persistObject(any());
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Agenda agenda = (Agenda)invocationOnMock.getArguments()[0];
                Agenda response =  new Agenda();
                response.setId(AGENDA_ID);
                ParamsMap paramsMap = new ParamsMap();
                paramsMap.put(GeneralParamKey.progressId, PROGRESS_ID);
                response.setParams(paramsMap);
                DataObjectResponse<Agenda> dataObjectResponse = new DefaultDataObjectResponse<>();
                dataObjectResponse.add(response);
                return dataObjectResponse;
            }
        }).when(mockAgendaClient).persistObject(any());


        DataObjectRequest request = new DefaultDataObjectRequest();
        ((DefaultDataObjectRequest) request).setDataObject(transformRequest);
        DataObjectResponse<TransformRequest> objectPersistResponse = transformRequestProcessor.handlePOST(request);
        TransformRequest responseObject = objectPersistResponse.getFirst();
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.progressId), PROGRESS_ID);
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.execProgressId), EXEC_PROGRESS_ID);
        Assert.assertEquals(responseObject.getParams().getString(GeneralParamKey.agendaId), AGENDA_ID);
    }

    protected TransformRequest createTransformRequest()
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setCustomerId(CUSTOMER_ID);
        return transformRequest;
    }
}
