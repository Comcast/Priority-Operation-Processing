package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
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
    private HttpURLConnectionFactory mockHttpURLConnectionFactory;
    private HttpCPObjectClient<AgendaProgress> mockAgendaProgressClient;
    private HttpCPObjectClient<Agenda> mockAgendaClient;

    @BeforeMethod
    public void setup()
    {
        mockTransformRequestPersister = mock(ObjectPersister.class);
        mockHttpURLConnectionFactory = mock(HttpURLConnectionFactory.class);
        mockAgendaProgressClient = mock(HttpCPObjectClient.class);
        mockAgendaClient = mock(HttpCPObjectClient.class);

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
                Object result = new ObjectPersistResponse(callCount > 0 ? EXEC_PROGRESS_ID : PROGRESS_ID);
                callCount++;
                return result;
            }
        }).when(mockAgendaProgressClient).persistObject(any());
        doAnswer(new Answer()
        {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                Agenda agenda = (Agenda)invocationOnMock.getArguments()[0];
                ObjectPersistResponse response =  new ObjectPersistResponse(AGENDA_ID);
                ParamsMap paramsMap = new ParamsMap();
                paramsMap.put(GeneralParamKey.progressId, PROGRESS_ID);
                response.setParams(paramsMap);
                return response;
            }
        }).when(mockAgendaClient).persistObject(any());


        ObjectPersistResponse objectPersistResponse = transformRequestProcessor.handlePOST(transformRequest);
        Assert.assertEquals(objectPersistResponse.getParams().getString(GeneralParamKey.progressId), PROGRESS_ID);
        Assert.assertEquals(objectPersistResponse.getParams().getString(GeneralParamKey.execProgressId), EXEC_PROGRESS_ID);
        Assert.assertEquals(objectPersistResponse.getParams().getString(GeneralParamKey.agendaId), AGENDA_ID);
    }

    protected TransformRequest createTransformRequest()
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setCustomerId(CUSTOMER_ID);
        return transformRequest;
    }
}
