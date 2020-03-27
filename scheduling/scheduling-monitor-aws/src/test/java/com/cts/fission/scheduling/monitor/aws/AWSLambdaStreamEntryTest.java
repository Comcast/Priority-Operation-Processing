package com.cts.fission.scheduling.monitor.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.cts.fission.scheduling.monitor.QueueMetricMonitor;
import com.cts.fission.scheduling.monitor.QueueMetricMonitorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.facility.ResourcePool;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentFacade;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.modules.monitor.graphite.GraphiteConfigKeys;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.endpoint.client.HttpObjectClientFactory;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class AWSLambdaStreamEntryTest
{
    private final String ENCRYPTED_PASS = "rkHt4ZZh+eprk0Vv+Q035g=="; // AES 'test'

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private EnvironmentLookupUtils mockEnvironmentLookupUtils = mock(EnvironmentLookupUtils.class);
    private EnvironmentFacade mockEnvironmentFacade = mock(EnvironmentFacade.class);
    private ObjectPersisterFactory<ReadyAgenda> mockReadyAgendaPersisterFactory = mock(ObjectPersisterFactory.class);
    private HttpObjectClientFactory mockHttpObjectClientFactory = mock(HttpObjectClientFactory.class);
    private QueueMetricMonitorFactory mockQueueMonitorFactory = mock(QueueMetricMonitorFactory.class);
    private QueueMetricMonitor mockQueueMonitor = mock(QueueMetricMonitor.class);
    private AWSLambdaStreamEntry streamEntry;

    @BeforeMethod
    public void setup()
    {
        Map<String, String> envVars = new HashMap<>();
        envVars.put(GraphiteConfigKeys.ENDPOINT.getPropertyKey(), "http://bananas.myendpoing.com/");
        doReturn(envVars).when(mockEnvironmentFacade).getEnv();
        HttpObjectClient mockReadyAgendaClient = mock(HttpObjectClient.class);
        doReturn(mockReadyAgendaClient).when(mockHttpObjectClientFactory).createClient(any(), any());
        DataObjectResponse dataObjectResponse = mock(DataObjectResponse.class);
        List resourcePools = Arrays.asList(new ResourcePool());
        doReturn(resourcePools).when(dataObjectResponse).getAll();
        doReturn(dataObjectResponse).when(mockReadyAgendaClient).getObjects(anyList());
        doReturn(mockQueueMonitor).when(mockQueueMonitorFactory).createQueueMonitor(any(), any(), any());
        streamEntry = new AWSLambdaStreamEntry(mockReadyAgendaPersisterFactory, mockHttpObjectClientFactory);
        streamEntry.setEnvironmentFacade(mockEnvironmentFacade);
        streamEntry.setQueueMonitorFactory(mockQueueMonitorFactory);
        streamEntry.setEnvironmentLookupUtils(mockEnvironmentLookupUtils);
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Failed to read input as ResourcePoolMonitorRequest.*")
    public void testHandleRequestInvalidInput()
    {
        // run some bad json
        callHandleRequest("{\"");
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Request must have a stageId.*")
    public void testHandleRequestNoInput()
    {
        // run some empty json
        callHandleRequest("{}");
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = ".*Missing environment var.*")
    public void testInvalidEnvVar() throws Exception
    {
        callHandleRequest(objectMapper.writeValueAsString(new ResourcePoolMonitorRequest().setResourcePoolId("a").setStageId("b")));
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = ".*Error processing resource pool.*")
    public void testProcessingError() throws Throwable
    {
        doReturn("valid").when(mockEnvironmentFacade).getEnv(anyString());
        doReturn(ENCRYPTED_PASS).when(mockEnvironmentLookupUtils).getEncryptedVarFromEnvironment(anyString());
        doThrow(new Exception()).when(mockQueueMonitor).monitor(anyString());
        callHandleRequest(objectMapper.writeValueAsString(new ResourcePoolMonitorRequest().setResourcePoolId("a").setStageId("b")));
        verify(mockQueueMonitor, times(1)).monitor(anyString());
    }

    private void callHandleRequest(String input)
    {
        streamEntry.handleRequest(
            new ByteArrayInputStream(input.getBytes()),
            mock(OutputStream.class),
            mock(Context.class)
        );
    }
}
