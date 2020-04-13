package com.comcast.pop.endpoint.agenda.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.endpoint.agenda.AgendaRequestProcessor;
import com.comcast.pop.endpoint.agenda.factory.AgendaFactory;
import com.comcast.pop.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.comcast.pop.endpoint.factory.RequestProcessorFactory;
import com.comcast.pop.endpoint.util.ServiceDataObjectRetriever;
import com.comcast.pop.endpoint.util.ServiceDataRequestResult;
import com.comcast.pop.endpoint.util.ServiceResponseFactory;
import com.comcast.pop.endpoint.api.DefaultServiceRequest;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.agenda.RunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RunAgendaResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectResponse;
import com.comcast.pop.object.api.IdentifiedObject;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class RunAgendaServiceRequestProcessorTest
{
    private static final String TEMPLATE_ID = "templateId";
    private static final String INVALID_JSON = "{";
    private static final String PAYLOAD_JSON = "{}";

    private ServiceResponseFactory<RunAgendaResponse> responseFactory = new ServiceResponseFactory<>(RunAgendaResponse.class);

    private RunAgendaServiceRequestProcessor requestProcessor;

    private RequestProcessorFactory mockRequestProcessorFactory;
    private AgendaTemplateRequestProcessor mockAgendaTemplateRequestProcessor;
    private AgendaRequestProcessor mockAgendaRequestProcessor;
    private ServiceDataObjectRetriever<RunAgendaResponse> mockDataObjectRetriever;
    private AgendaFactory mockAgendaFactory;

    @BeforeMethod
    public void setup()
    {
        mockRequestProcessorFactory = mock(RequestProcessorFactory.class);
        mockAgendaTemplateRequestProcessor = mock(AgendaTemplateRequestProcessor.class);
        mockAgendaRequestProcessor = mock(AgendaRequestProcessor.class);
        mockAgendaFactory = mock(AgendaFactory.class);
        mockDataObjectRetriever = mock(ServiceDataObjectRetriever.class);
        doReturn(mockAgendaRequestProcessor).when(mockRequestProcessorFactory).createAgendaRequestProcessor(
            any(), any(), any(), any(), any(), any());
        doReturn(mockAgendaTemplateRequestProcessor).when(mockRequestProcessorFactory).createAgendaTemplateRequestProcessor(any());
        requestProcessor = new RunAgendaServiceRequestProcessor(null, null, null, null, null, null, null);
        requestProcessor.setRequestProcessorFactory(mockRequestProcessorFactory);
        requestProcessor.setAgendaFactory(mockAgendaFactory);
        requestProcessor.setDataObjectRetriever(mockDataObjectRetriever);
    }

    @DataProvider
    public Object[][] invalidAgendaTemplateIdProvider()
    {
        return new Object[][]
            {
                {null},
                {""},
                {" "},
            };
    }

    @Test(dataProvider = "invalidAgendaTemplateIdProvider")
    public void testInvalidAgendaTemplateId(String agendaTemplateId)
    {
        try
        {
            // go through the parent method so validator is used
            requestProcessor.handlePOST(createServiceRequest(agendaTemplateId, null));
            Assert.fail(String.format("%1$s should have been thrown.", ValidationException.class.getSimpleName()));
        }
        catch(ValidationException e)
        {
            Assert.assertTrue(StringUtils.containsIgnoreCase(e.getMessage(), RunAgendaServiceRequestValidator.REQUIRED_PARAMS_MISSING));
        }
    }

    @Test
    public void testInvalidJsonInput()
    {
        ServiceRequest<RunAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, INVALID_JSON);
        verifyFailure(serviceRequest, RunAgendaServiceRequestProcessor.INVALID_JSON_PAYLOAD);
        verify(mockDataObjectRetriever, times(0)).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
    }

    @Test
    public void testAgendaTemplateLookupFail()
    {
        final String NOT_FOUND = "not found";
        ServiceRequest<RunAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
        doReturn(createServiceDataRequestResult(null, responseFactory.createResponse(serviceRequest, ErrorResponseFactory.objectNotFound(NOT_FOUND, ""), null)))
            .when(mockDataObjectRetriever).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        verifyFailure(serviceRequest, NOT_FOUND);
        verify(mockDataObjectRetriever, times(1)).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
    }

    @Test
    public void testAgendaSubmitFail()
    {
        final String UNKNOWN_ERROR = "unknown error";
        ServiceRequest<RunAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
        doReturn(createServiceDataRequestResult(new AgendaTemplate(), null))
            .when(mockDataObjectRetriever).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        doReturn(new Agenda()).when(mockAgendaFactory).createAgendaFromObject(any(), any(), any(), any());
        doReturn(new DefaultDataObjectResponse<Agenda>(ErrorResponseFactory.runtimeServiceException(UNKNOWN_ERROR, null))).when(mockAgendaRequestProcessor).handlePOST(any());
        verifyFailure(serviceRequest, UNKNOWN_ERROR);
        verify(mockDataObjectRetriever, times(1)).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        verify(mockAgendaRequestProcessor, times(1)).handlePOST(any());
    }

    @Test
    public void testAgendaSubmitSuccess()
    {
        ServiceRequest<RunAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
        doReturn(createServiceDataRequestResult(new AgendaTemplate(), null))
            .when(mockDataObjectRetriever).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        doReturn(new Agenda()).when(mockAgendaFactory).createAgendaFromObject(any(), any(), any(), any());
        doReturn(new DefaultDataObjectResponse<Agenda>()).when(mockAgendaRequestProcessor).handlePOST(any());
        RunAgendaResponse response = requestProcessor.handlePOST(serviceRequest);
        Assert.assertFalse(response.isError());
        verify(mockDataObjectRetriever, times(1)).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        verify(mockAgendaRequestProcessor, times(1)).handlePOST(any());
    }

    private void verifyFailure(ServiceRequest<RunAgendaRequest> serviceRequest, String expectedMessage)
    {
        RunAgendaResponse response = requestProcessor.handlePOST(serviceRequest);
        Assert.assertTrue(response.isError());
        if(expectedMessage != null)
        {
            Assert.assertTrue(StringUtils.containsIgnoreCase(response.getErrorResponse().getDescription(), expectedMessage));
        }
    }

    private <D extends IdentifiedObject> ServiceDataRequestResult<D, RunAgendaResponse> createServiceDataRequestResult(
        D dataObject, RunAgendaResponse serviceResponse)
    {
        ServiceDataRequestResult<D, RunAgendaResponse> response = new ServiceDataRequestResult<>();
        response.setServiceResponse(serviceResponse);

        if(dataObject != null)
        {
            DefaultDataObjectResponse<D> objectResponse = new DefaultDataObjectResponse<>();
            objectResponse.add(dataObject);
            response.setDataObjectResponse(objectResponse);
        }
        return response;
    }

    private ServiceRequest<RunAgendaRequest> createServiceRequest(String agendaTemplateId, String payload)
    {
        RunAgendaRequest runAgendaRequest = new RunAgendaRequest();
        runAgendaRequest.setAgendaTemplateId(agendaTemplateId);
        runAgendaRequest.setPayload(payload);
        return new DefaultServiceRequest<>(runAgendaRequest);
    }

}
