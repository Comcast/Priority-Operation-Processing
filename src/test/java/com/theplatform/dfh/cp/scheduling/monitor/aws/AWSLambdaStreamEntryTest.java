package com.theplatform.dfh.cp.scheduling.monitor.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.agenda.reclaim.aws.AWSLambdaStreamEntry;
import com.theplatform.dfh.cp.agenda.reclaim.config.ReclaimerConfig;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.modules.queue.api.ItemQueueFactory;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class AWSLambdaStreamEntryTest
{
    private final String ENCRYPTED_PASS = "rkHt4ZZh+eprk0Vv+Q035g=="; // AES 'test'

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private EnvironmentLookupUtils mockEnvironmentLookupUtils = mock(EnvironmentLookupUtils.class);
    private EnvironmentFacade mockEnvironmentFacade = mock(EnvironmentFacade.class);
    private ItemQueueFactory<ReadyAgenda> mockReadyAgendaQueueFactory = mock(ItemQueueFactory.class);
    private ObjectPersisterFactory<ReadyAgenda> mockReadyAgendaPersisterFactory = mock(ObjectPersisterFactory.class);
    private AWSLambdaStreamEntry streamEntry;

    @BeforeMethod
    public void setup()
    {
        streamEntry.setEnvironmentFacade(mockEnvironmentFacade);
        streamEntry.setEnvironmentLookupUtils(mockEnvironmentLookupUtils);
    }

    //@Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Failed to read input as ResourcePoolSchedulerRequest.*")
    public void testHandleRequestInvalidInput() throws IOException
    {
        // run some bad json
        callHandleRequest("{\"");
    }

    //@Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Request must have a facilityId and stageId.*")
    public void testHandleRequestNoInput() throws IOException
    {
        // run some empty json
        callHandleRequest("{}");
    }

    //@Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Missing environment var.*")
    public void testInvalidEnvVar() throws Exception
    {
        callHandleRequest(objectMapper.writeValueAsString(new ReclaimerConfig()));
    }

    //@Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Error processing resource pool.*")
    public void testProcessingError() throws Throwable
    {
        doReturn("valid").when(mockEnvironmentFacade).getEnv(anyString());
        doReturn(ENCRYPTED_PASS).when(mockEnvironmentLookupUtils).getEncryptedVarFromEnvironment(anyString());
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
