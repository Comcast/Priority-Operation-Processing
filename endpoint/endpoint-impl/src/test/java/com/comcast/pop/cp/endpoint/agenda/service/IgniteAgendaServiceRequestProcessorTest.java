package com.comcast.pop.cp.endpoint.agenda.service;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.cp.endpoint.agenda.AgendaRequestProcessor;
import com.comcast.pop.cp.endpoint.agenda.factory.AgendaFactory;
import com.comcast.pop.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.comcast.pop.cp.endpoint.factory.RequestProcessorFactory;
import com.comcast.pop.cp.endpoint.util.ServiceDataObjectRetriever;
import com.comcast.pop.cp.endpoint.util.ServiceDataRequestResult;
import com.comcast.pop.cp.endpoint.util.ServiceResponseFactory;
import com.comcast.pop.endpoint.api.DefaultServiceRequest;
import com.comcast.pop.endpoint.api.ErrorResponseFactory;
import com.comcast.pop.endpoint.api.ServiceRequest;
import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.agenda.IgniteAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.IgniteAgendaResponse;
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

public class IgniteAgendaServiceRequestProcessorTest
{
    private static final String TEMPLATE_ID = "templateId";
    private static final String INVALID_JSON = "{";
    private static final String PAYLOAD_JSON = "{}";

    private ServiceResponseFactory<IgniteAgendaResponse> responseFactory = new ServiceResponseFactory<>(IgniteAgendaResponse.class);

    private IgniteAgendaServiceRequestProcessor requestProcessor;

    private RequestProcessorFactory mockRequestProcessorFactory;
    private AgendaTemplateRequestProcessor mockAgendaTemplateRequestProcessor;
    private AgendaRequestProcessor mockAgendaRequestProcessor;
    private ServiceDataObjectRetriever<IgniteAgendaResponse> mockDataObjectRetriever;
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
        requestProcessor = new IgniteAgendaServiceRequestProcessor(null, null, null, null, null, null, null);
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
            Assert.assertTrue(StringUtils.containsIgnoreCase(e.getMessage(), IgniteAgendaServiceRequestValidator.REQUIRED_PARAMS_MISSING));
        }
    }

    @Test
    public void testInvalidJsonInput()
    {
        ServiceRequest<IgniteAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, INVALID_JSON);
        verifyFailure(serviceRequest, IgniteAgendaServiceRequestProcessor.INVALID_JSON_PAYLOAD);
        verify(mockDataObjectRetriever, times(0)).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
    }

    @Test
    public void testAgendaTemplateLookupFail()
    {
        final String NOT_FOUND = "not found";
        ServiceRequest<IgniteAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
        doReturn(createServiceDataRequestResult(null, responseFactory.createResponse(serviceRequest, ErrorResponseFactory.objectNotFound(NOT_FOUND, ""), null)))
            .when(mockDataObjectRetriever).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        verifyFailure(serviceRequest, NOT_FOUND);
        verify(mockDataObjectRetriever, times(1)).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
    }

    @Test
    public void testAgendaSubmitFail()
    {
        final String UNKNOWN_ERROR = "unknown error";
        ServiceRequest<IgniteAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
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
        ServiceRequest<IgniteAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
        doReturn(createServiceDataRequestResult(new AgendaTemplate(), null))
            .when(mockDataObjectRetriever).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        doReturn(new Agenda()).when(mockAgendaFactory).createAgendaFromObject(any(), any(), any(), any());
        doReturn(new DefaultDataObjectResponse<Agenda>()).when(mockAgendaRequestProcessor).handlePOST(any());
        IgniteAgendaResponse response = requestProcessor.handlePOST(serviceRequest);
        Assert.assertFalse(response.isError());
        verify(mockDataObjectRetriever, times(1)).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        verify(mockAgendaRequestProcessor, times(1)).handlePOST(any());
    }

    private void verifyFailure(ServiceRequest<IgniteAgendaRequest> serviceRequest, String expectedMessage)
    {
        IgniteAgendaResponse response = requestProcessor.handlePOST(serviceRequest);
        Assert.assertTrue(response.isError());
        if(expectedMessage != null)
        {
            Assert.assertTrue(StringUtils.containsIgnoreCase(response.getErrorResponse().getDescription(), expectedMessage));
        }
    }

    private <D extends IdentifiedObject> ServiceDataRequestResult<D, IgniteAgendaResponse> createServiceDataRequestResult(
        D dataObject, IgniteAgendaResponse serviceResponse)
    {
        ServiceDataRequestResult<D, IgniteAgendaResponse> response = new ServiceDataRequestResult<>();
        response.setServiceResponse(serviceResponse);

        if(dataObject != null)
        {
            DefaultDataObjectResponse<D> objectResponse = new DefaultDataObjectResponse<>();
            objectResponse.add(dataObject);
            response.setDataObjectResponse(objectResponse);
        }
        return response;
    }

    private ServiceRequest<IgniteAgendaRequest> createServiceRequest(String agendaTemplateId, String payload)
    {
        IgniteAgendaRequest igniteAgendaRequest = new IgniteAgendaRequest();
        igniteAgendaRequest.setAgendaTemplateId(agendaTemplateId);
        igniteAgendaRequest.setPayload(payload);
        return new DefaultServiceRequest<>(igniteAgendaRequest);
    }

}