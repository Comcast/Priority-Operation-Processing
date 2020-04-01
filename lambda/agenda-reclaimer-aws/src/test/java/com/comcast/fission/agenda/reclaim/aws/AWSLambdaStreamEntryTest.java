package com.comcast.fission.agenda.reclaim.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.comcast.fission.agenda.reclaim.AgendaReclaimer;
import com.comcast.fission.agenda.reclaim.config.ReclaimerConfig;
import com.comcast.fission.agenda.reclaim.factory.AgendaReclaimerFactory;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.comcast.fission.endpoint.api.BadRequestException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class AWSLambdaStreamEntryTest
{
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String AGENDA_PROGRESS_ENDPOINT_URL = "http://127.0.0.1";
    private static final String DEFAULT_ENV_VAR_VALUE = "default";

    private EnvironmentLookupUtils mockEnvironmentLookupUtils ;
    private EnvironmentFacade mockEnvironmentFacade;
    private AgendaReclaimerFactory mockAgendaReclaimerFactory;
    private AgendaReclaimer mockAgendaReclaimer;
    private AWSLambdaStreamEntry streamEntry;

    @BeforeMethod
    public void setup()
    {
        streamEntry = new AWSLambdaStreamEntry();

        mockEnvironmentLookupUtils = mock(EnvironmentLookupUtils.class);
        mockEnvironmentFacade = mock(EnvironmentFacade.class);
        mockAgendaReclaimerFactory = mock(AgendaReclaimerFactory.class);
        mockAgendaReclaimer = mock(AgendaReclaimer.class);

        doReturn(DEFAULT_ENV_VAR_VALUE).when(mockEnvironmentFacade).getEnv(anyString());
        doReturn("unknown").when(mockEnvironmentLookupUtils).getEncryptedVarFromEnvironment(anyString());
        doReturn(mockAgendaReclaimer).when(mockAgendaReclaimerFactory).createAgendaReclaimer(any(), any(), any());

        streamEntry.setEnvironmentFacade(mockEnvironmentFacade);
        streamEntry.setEnvironmentLookupUtils(mockEnvironmentLookupUtils);
        streamEntry.setAgendaReclaimerFactory(mockAgendaReclaimerFactory);
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Failed to read input as ReclaimerConfig.*")
    public void testHandleRequestInvalidInput() throws IOException
    {
        // run some bad json
        callHandleRequest("{\"");
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Input not configured correctly\\. No parameters were specified.*")
    public void testHandleRequestNoInput() throws IOException
    {
        // run some empty json
        callHandleRequest("null");
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Missing environment var.*")
    public void testInvalidEnvVar() throws Exception
    {
        doReturn(null).when(mockEnvironmentFacade).getEnv(anyString());
        callHandleRequest(objectMapper.writeValueAsString(new ReclaimerConfig()));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Agenda reclaim processing failed.*")
    public void testProcessingException() throws Exception
    {
        doThrow(new RuntimeException("bad")).when(mockAgendaReclaimer).process();
        callHandleRequest(objectMapper.writeValueAsString(new ReclaimerConfig()));
    }

    private void callHandleRequest(String input) throws IOException
    {
        streamEntry.handleRequest(
            new ByteArrayInputStream(input.getBytes()),
            mock(OutputStream.class),
            mock(Context.class)
        );
    }
}
