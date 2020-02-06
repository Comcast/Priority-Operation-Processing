package com.theplatform.dfh.cp.endpoint.agenda.service;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agenda.factory.AgendaFactory;
import com.theplatform.dfh.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.theplatform.dfh.cp.endpoint.factory.RequestProcessorFactory;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataObjectRetriever;
import com.theplatform.dfh.cp.endpoint.util.ServiceDataRequestResult;
import com.theplatform.dfh.cp.endpoint.util.ServiceResponseFactory;
import com.theplatform.dfh.endpoint.api.DefaultServiceRequest;
import com.theplatform.dfh.endpoint.api.ErrorResponseFactory;
import com.theplatform.dfh.endpoint.api.ServiceRequest;
import com.theplatform.dfh.endpoint.api.ValidationException;
import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaRequest;
import com.theplatform.dfh.endpoint.api.agenda.service.SubmitAgendaResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.object.api.IdentifiedObject;
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

public class SubmitAgendaServiceRequestProcessorTest
{
    private static final String TEMPLATE_ID = "templateId";
    private static final String INVALID_JSON = "{";
    private static final String PAYLOAD_JSON = "{}";

    private ServiceResponseFactory<SubmitAgendaResponse> responseFactory = new ServiceResponseFactory<>(SubmitAgendaResponse.class);

    private SubmitAgendaServiceRequestProcessor requestProcessor;

    private RequestProcessorFactory mockRequestProcessorFactory;
    private AgendaTemplateRequestProcessor mockAgendaTemplateRequestProcessor;
    private AgendaRequestProcessor mockAgendaRequestProcessor;
    private ServiceDataObjectRetriever<SubmitAgendaResponse> mockDataObjectRetriever;
    private AgendaFactory mockAgendaFactory;

    @BeforeMethod
    public void setup()
    {
        mockRequestProcessorFactory = mock(RequestProcessorFactory.class);
        mockAgendaTemplateRequestProcessor = mock(AgendaTemplateRequestProcessor.class);
        mockAgendaRequestProcessor = mock(AgendaRequestProcessor.class);
        mockAgendaFactory = mock(AgendaFactory.class);
        mockDataObjectRetriever = mock(ServiceDataObjectRetriever.class);
        doReturn(mockAgendaRequestProcessor).when(mockRequestProcessorFactory).createAgendaRequestProcessorWithServiceRequestVisibility(
            any(), any(), any(), any(), any(), any(), any());
        doReturn(mockAgendaTemplateRequestProcessor).when(mockRequestProcessorFactory).createAgendaTemplateRequestProcessor(any());
        requestProcessor = new SubmitAgendaServiceRequestProcessor(null, null, null, null, null, null, null);
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
            Assert.assertTrue(StringUtils.containsIgnoreCase(e.getMessage(), SubmitAgendaRequestValidator.REQUIRED_PARAMS_MISSING));
        }
    }

    @Test
    public void testInvalidJsonInput()
    {
        ServiceRequest<SubmitAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, INVALID_JSON);
        verifyFailure(serviceRequest, SubmitAgendaServiceRequestProcessor.INVALID_JSON_PAYLOAD);
        verify(mockDataObjectRetriever, times(0)).performObjectRetrieve(any(), any(), any(), any());
    }

    @Test
    public void testAgendaTemplateLookupFail()
    {
        final String NOT_FOUND = "not found";
        ServiceRequest<SubmitAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
        doReturn(createServiceDataRequestResult(null, responseFactory.createResponse(serviceRequest, ErrorResponseFactory.objectNotFound(NOT_FOUND, ""), null)))
            .when(mockDataObjectRetriever).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        verifyFailure(serviceRequest, NOT_FOUND);
        verify(mockDataObjectRetriever, times(1)).performObjectRetrieve(any(), any(), any(), any());
    }

    @Test
    public void testAgendaSubmitFail()
    {
        final String UNKNOWN_ERROR = "unknown error";
        ServiceRequest<SubmitAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
        doReturn(createServiceDataRequestResult(new AgendaTemplate(), null))
            .when(mockDataObjectRetriever).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        doReturn(new Agenda()).when(mockAgendaFactory).createAgendaFromObject(any(), any(), any(), any());
        doReturn(new DefaultDataObjectResponse<Agenda>(ErrorResponseFactory.runtimeServiceException(UNKNOWN_ERROR, null))).when(mockAgendaRequestProcessor).handlePOST(any());
        verifyFailure(serviceRequest, UNKNOWN_ERROR);
        verify(mockDataObjectRetriever, times(1)).performObjectRetrieve(any(), any(), any(), any());
        verify(mockAgendaRequestProcessor, times(1)).handlePOST(any());
    }

    @Test
    public void testAgendaSubmitSuccess()
    {
        ServiceRequest<SubmitAgendaRequest> serviceRequest = createServiceRequest(TEMPLATE_ID, PAYLOAD_JSON);
        doReturn(createServiceDataRequestResult(new AgendaTemplate(), null))
            .when(mockDataObjectRetriever).performObjectRetrieve(serviceRequest, mockAgendaTemplateRequestProcessor, TEMPLATE_ID, AgendaTemplate.class);
        doReturn(new Agenda()).when(mockAgendaFactory).createAgendaFromObject(any(), any(), any(), any());
        doReturn(new DefaultDataObjectResponse<Agenda>()).when(mockAgendaRequestProcessor).handlePOST(any());
        SubmitAgendaResponse response = requestProcessor.handlePOST(serviceRequest);
        Assert.assertFalse(response.isError());
        verify(mockDataObjectRetriever, times(1)).performObjectRetrieve(any(), any(), any(), any());
        verify(mockAgendaRequestProcessor, times(1)).handlePOST(any());
    }

    private void verifyFailure(ServiceRequest<SubmitAgendaRequest> serviceRequest, String expectedMessage)
    {
        SubmitAgendaResponse response = requestProcessor.handlePOST(serviceRequest);
        Assert.assertTrue(response.isError());
        if(expectedMessage != null)
        {
            Assert.assertTrue(StringUtils.containsIgnoreCase(response.getErrorResponse().getDescription(), expectedMessage));
        }
    }

    private <D extends IdentifiedObject> ServiceDataRequestResult<D, SubmitAgendaResponse> createServiceDataRequestResult(
        D dataObject, SubmitAgendaResponse serviceResponse)
    {
        ServiceDataRequestResult<D, SubmitAgendaResponse> response = new ServiceDataRequestResult<>();
        response.setServiceResponse(serviceResponse);

        if(dataObject != null)
        {
            DefaultDataObjectResponse<D> objectResponse = new DefaultDataObjectResponse<>();
            objectResponse.add(dataObject);
            response.setDataObjectResponse(objectResponse);
        }
        return response;
    }

    private ServiceRequest<SubmitAgendaRequest> createServiceRequest(String agendaTemplateId, String payload)
    {
        SubmitAgendaRequest submitAgendaRequest = new SubmitAgendaRequest();
        submitAgendaRequest.setAgendaTemplateId(agendaTemplateId);
        submitAgendaRequest.setPayload(payload);
        return new DefaultServiceRequest<>(submitAgendaRequest);
    }

}
